package org.praekelt.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.javarosa.core.api.State;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.DateData;
//import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.xform.parse.XFormParser;
import org.praekelt.xforms.Event;
import org.praekelt.xforms.Lock;
import org.praekelt.xforms.Persistence;
import org.praekelt.xforms.SequencingException;

/**
 * The object that will contain the xform state
 *
 * @author victorgeere
 */
public class RosaFactory implements Serializable {

    private boolean persist;
    private String uuid;
    private Lock lock;
    private String navMode;
    private int seqId;
    private FormDef form;
    private FormEntryModel fem;
    private FormEntryController fec;
    private Event currentEvent;
    private Date lastActivity;

    public RosaFactory(String uuid, String navMode, int seqId, String xform,
            String instance, String extensions, String sessionData, String apiAuth, String initLang, int curIndex) {
        this.uuid = uuid;
        this.lock = new Lock();
        this.navMode = navMode;
        this.seqId = seqId;

//        this.form = this.loadForm(xform, instance, params.get('extensions', []), params.get('session_data', {}), params.get('api_auth'));
        this.form = this.loadForm(xform, instance, extensions, sessionData, apiAuth);
        this.fem = new FormEntryModel(this.form, FormEntryModel.REPEAT_STRUCTURE_NON_LINEAR);
        this.fec = new FormEntryController(this.fem);
        if (initLang != null) {
            try {
                this.fec.setLanguage(initLang);
            } catch (UnregisteredLocaleException ule) {
                // pass # just use default language
            }
        }

        if (curIndex > 0) {
            this.fec.jumpToIndex(this.parseIx(curIndex));
        }

        this.parseCurrentEvent();

        /*

         this.staleness_window = 3600. * params['staleness_window'
         ]
         this.persist = params.get('persist', settings.PERSIST_SESSIONS
         )
         this.orig_params = {
         'xform': xform,
         'nav_mode'
         : params.get('nav_mode'),
         'session_data'
         : params.get('session_data'),
         'api_auth'
         : params.get('api_auth'),
         'staleness_window': params['staleness_window'
         ],
         }
         this.update_last_activity()
         */
    }

    /**
     * Get a RosaFactory object, deal with deserialization
     *
     * @return
     */
    public static RosaFactory getInstance() {
        return new RosaFactory("", "", 0, "", "", "", "", "", "", 0);
    }

    /**
     * Serialize a form
     *
     * @param form
     * @return
     */
    public String serializeForm(FormDef form) {
        String s = "";
        try {
            XFormSerializingVisitor fs = new XFormSerializingVisitor();
            byte[] ba = fs.serializeInstance(form.getInstance());
            s = new String(ba, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(RosaFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
        return s;
    }

    /**
     * Serialize this object if it needs to be cached in Redis
     *
     * @param obj
     * @return
     */
    public String serialize() {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bout);
            out.writeObject(this);
            out.close();
            bout.close();
        } catch (IOException ex) {
            Logger.getLogger(RosaFactory.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                out.close();
            } catch (IOException ex) {
                Logger.getLogger(RosaFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return bout.toString();
    }

    /**
     * Instantiate a form
     *
     * @param xform
     * @param instance
     * @param extensions
     * @param sessionData
     * @param apiAuth
     * @return
     */
    public FormDef loadForm(String xform, String instance, String extensions, String sessionData, String apiAuth) {
        return this.loadForm(xform, instance);
    }

    /**
     * Instantiate a form
     *
     * @param xform
     * @param instance
     * @param extensions
     * @param sessionData
     * @param apiAuth
     * @return
     */
    public FormDef loadForm(String xform, String instance) {
        Reader xformReader = (Reader) new StringReader(xform);
        XFormParser xfp;
        xfp = new XFormParser(xformReader);
        FormDef form = xfp.parse();
        if (instance != null) {
            StringReader sr = new StringReader(instance);
            xfp = new XFormParser(sr);
            xfp.loadXmlInstance(form, sr);
        }
        return form;
    }

    /**
     *
     * @return
     */
    public RosaFactory enter() throws SequencingException {
        if (this.navMode.equalsIgnoreCase("fao")) {
            this.lock.acquire();
        } else {
            if (!this.lock.acquire(false)) {
                throw new SequencingException();
            }
        }
        this.seqId++;
        this.updateLastActivity();
        return this;
    }

    /**
     * 
     */
    public void exit() {
        if (this.persist) {
            //# TODO should this be done async? we must dump state before releasing the lock, however
            Persistence.persist(this);
        }
        this.lock.release();
    }

    /**
     * Set when the last activity took place
     */
    public void updateLastActivity() {
             this.lastActivity = new Date();//time.time();
    }

    public void sessionState() {
        State  state = dict(this.origParams);
        state.update({
            'instance': self.output(),
            'init_lang': self.get_lang(),
            'cur_index': str(self.fem.getFormIndex()) if self.nav_mode != 'fao' else None,
            'seq_id': self.seq_id,
        });
        //# prune entries with null value, so that defaults will take effect when the session is re-created
        state = dict((k, v) for k, v in state.iteritems() if v is not null);
        return state
    }

    public void output() {
    }

    public void walk() {
    }

    public void ixInScope() {
    }

    private Event parseCurrentEvent() {
        this.currentEvent = this.parseEvent(this.fem.getFormIndex());
        return this.currentEvent;
    }

    /**
     * 
     * @param formIx
     * @return 
     */
    public Event parseEvent(FormIndex formIx) {
        Event event = new Event(); //{'ix': form_ix};

        event.setFi(formIx);

        int status = this.fem.getEvent(formIx);

        if (status == this.fec.EVENT_BEGINNING_OF_FORM) {
            event.setType("form-start");
        } else if (status == this.fec.EVENT_END_OF_FORM) {
            event.setType("form-complete");
        } else if (status == this.fec.EVENT_QUESTION) {
            event.setType("question");
            this.parseQuestion(event);
        } else if (status == this.fec.EVENT_REPEAT_JUNCTURE) {
            event.setType("repeat-juncture");
            this.parseRepeatJuncture(event);
        } else {
            event.setType("sub-group");
            FormEntryCaption prompt = this.fem.getCaptionPrompt(formIx);
            event.setCaption(prompt.getLongText());
            event.setCaptionAudio(prompt.getAudioText());
            event.setCaptionImage(prompt.getImageText());
            //FormEntryPrompt.TEXT_FORM_VIDEO
            //event.captionVideo(prompt.getSpecialFormQuestionText(FormEntryPrompt.TEXT_FORM_VIDEO));
            if (status == this.fec.EVENT_GROUP) {

                event.setRepeatable(false);
            } else if (status == this.fec.EVENT_REPEAT) {

                event.setRepeatable(true);
                event.setExists(true);
            } else if (status == this.fec.EVENT_PROMPT_NEW_REPEAT) {
                //obsolete 
                event.setRepeatable(true);
                event.setExists(false);
            }
        }
        return event;
    }

    public void getQuestionChoices() {
    }

    public void parseStyleInfo() {
    }

    public void parseQuestion() {
    }

    public void parseRepeatJuncture() {
    }

    /**
     *
     * @return
     */
    public Event nextEvent() {
        this.fec.stepToNextEvent();
        return this.parseCurrentEvent();
    }

    /**
     *
     * @return
     */
    public Event backEvent() {
        this.fec.stepToPreviousEvent();
        return this.parseCurrentEvent();
    }

    public void answerQuestion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void loadFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void getLoader() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void initContext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void openForm() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void editRepeat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void newRepeat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void deleteRepeat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void newRepitition() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void skipNext() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void goBack() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void submitForm() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void setLocale() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void currentQuestion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void heartbeat() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void prevEvent() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void saveForm() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void formCompletion() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void purge() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private FormIndex parseIx(int curIndex) {
        FormIndex fi = null;
        return fi;
    }

    private void parseQuestion(Event event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void parseRepeatJuncture(Event event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
