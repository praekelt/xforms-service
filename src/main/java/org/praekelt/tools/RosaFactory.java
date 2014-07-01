package org.praekelt.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.sql.Types;
import java.util.Date;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.javarosa.core.api.State;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.DateData;
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
import org.praekelt.xforms.Params;
import org.praekelt.xforms.Persistence;
import org.praekelt.xforms.SequencingException;
import org.praekelt.xforms.Status;
import org.praekelt.xforms.ValueException;
import org.praekelt.xforms.XTypes;

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
    private Date lastActivity;
    private final int stalenessWindow;
    private final Params origParams;
    private Event curEvent;
    private Object answer;
    private int datatype;
    private Object ix;
    private Object q;

    /**
     *
     * @param uuid
     * @param navMode
     * @param seqId
     * @param xform
     * @param instance
     * @param extensions
     * @param sessionData
     * @param apiAuth
     * @param initLang
     * @param curIndex
     * @param persist
     * @param stalenessWindow
     */
    public RosaFactory(String uuid, String navMode, int seqId, String xform,
            String instance, String extensions, String sessionData,
            String apiAuth, String initLang, int curIndex, boolean persist, int stalenessWindow) {

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

        this.stalenessWindow = 3600 * stalenessWindow;

        this.persist = persist; //params.get('persist', settings.PERSIST_SESSIONS);

        this.origParams = new Params(xform, navMode, sessionData, apiAuth, stalenessWindow);

        this.updateLastActivity();

    }

    /**
     * Get a RosaFactory object, deal with deserialization
     *
     * @return
     */
    public static RosaFactory getInstance() {
        return new RosaFactory("", "", 0, "", "", "", "", "", "", 0, true, 1);
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

    /**
     *
     */
    public State sessionState() {
        Params state = this.origParams;
        /*
         state.update({
         'instance': this.output(),
         'init_lang': this.get_lang(),
         'cur_index': str(this.fem.getFormIndex()) if this.nav_mode != 'fao' else None,
         'seq_id': this.seq_id,
         });
         //# prune entries with null value, so that defaults will take effect when the session is re-created
         state = dict((k, v) for k, v in state.iteritems() if v is not null);
         */
        return state;
    }

    public void output() {
        /*
         if this.cur_event['type'] != 'form-complete':
         #warn that not at end of form
         pass

         instance_bytes = FormSerializer().serializeInstance(this.form.getInstance())
         return unicode(''.join(chr(b) for b in instance_bytes.tolist()), 'utf-8')        
         */
    }

    public void walk() {
        /*
         form_ix = FormIndex.createBeginningOfFormIndex()
         tree = []
         this._walk(form_ix, tree)
         return tree
         */
    }

    public void walk(int parentIx, int[] siblings) {
        /*
         def _walk(self, parent_ix, siblings):
         def step(ix, descend):
         next_ix = this.fem.incrementIndex(ix, descend)
         this.fem.setQuestionIndex(next_ix)  # needed to trigger events in form engine
         return next_ix 
        
         def ix_in_scope(form_ix):
         if form_ix.isEndOfFormIndex():
         return False
         elif parent_ix.isBeginningOfFormIndex():
         return True
         else:
         return FormIndex.isSubElement(parent_ix, form_ix)

         form_ix = step(parent_ix, True)
         while ix_in_scope(form_ix):
         relevant = this.fem.isIndexRelevant(form_ix)

         if not relevant:
         form_ix = step(form_ix, False)
         continue

         evt = this.__parse_event(form_ix)
         evt['relevant'] = relevant
         if evt['type'] == 'sub-group':
         presentation_group = (evt['caption'] != None)
         if presentation_group:
         siblings.append(evt)
         evt['children'] = []
         form_ix = this._walk(form_ix, evt['children'] if presentation_group else siblings)
         elif evt['type'] == 'repeat-juncture':
         siblings.append(evt)
         evt['children'] = []
         for i in range(0, this.fem.getForm().getNumRepetitions(form_ix)):
         subevt = {
         'type': 'sub-group',
         'ix': this.fem.getForm().descendIntoRepeat(form_ix, i),
         'caption': evt['repetitions'][i],
         'repeatable': True,
         'children': [],
         }

         # kinda ghetto; we need to be able to track distinct repeat instances, even if their position
         # within the list of repetitions changes (such as by deleting a rep in the middle)
         # would be nice to have proper FormEntryAPI support for this
         java_uid = this.form.getInstance().resolveReference(subevt['ix'].getReference()).hashCode()
         subevt['uuid'] = hashlib.sha1(str(java_uid)).hexdigest()[:12]

         evt['children'].append(subevt)
         this._walk(subevt['ix'], subevt['children'])
         for key in ['repetitions', 'del-choice', 'del-header', 'done-choice']:
         del evt[key]
         form_ix = step(form_ix, True) # why True?
         else:
         siblings.append(evt)
         form_ix = step(form_ix, True) # why True?

         return form_ix
        
         */

    }

    /**
     * Parse the current event
     *
     * @return
     */
    private Event parseCurrentEvent() {
        this.curEvent = this.parseEvent(this.fem.getFormIndex());
        return this.curEvent;
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

    /**
     *
     *
     * @param answer
     * @param ix
     */
    public Status answerQuestion(Object answer, FormIndex ix) throws ValueException {
        ix = this.parseIx(ix);
        Event event = this.curEvent;

        if (ix != null) {
            this.parseEvent(ix);
        }

        if (0 != event.getType().compareToIgnoreCase("question")) {
            throw new ValueException("not currently on a question");
        }

        int datatype = event.getDataType();
        if (datatype == Event.UNRECOGNISED) {

        }

//         # don"t commit answers to unrecognized questions, since we
//         # couldn"t parse what was originally there. whereas for
//         # _unsupported_ questions, we"re parsing and re-committing the
//         # answer verbatim
        return new Status("success");
    }

    /**
     *
     * @param a
     * @return
     */
    public Status multians(String[] a) {

        //TODO: causes unreachable statement
        /**
         * if (hasattr(a, "__iter__")) { return a; } else { return
         * str(a).split(" "); }
         */
        Object ans;
        if (answer == null || str(answer).trim() == "" /**
                 * || answer == []
                 */
                ) {
            ans = null;
        } else if (datatype == XTypes.INT) {
            ans = IntegerData(Integer.valueOf((String) answer));
        } else if (datatype == XTypes.LONG) {
            ans = LongData(Long.valueOf((String) answer));
        } else if (datatype == XTypes.FLOAT) {
            ans = DecimalData(Float.valueOf((String) answer));
        } else if (datatype == XTypes.STRING | datatype == XTypes.INFO) {
            ans = StringData(str((String[]) answer));
        } else if (datatype == XTypes.DATE) {
            //TODO: convert date to string
//            ans = DateData(to_jdate(datetime.strptime(str(answer), "%Y-%m-%d").date()));
        } else if (datatype == XTypes.TIME) {
            //TODO: cast time
//            ans = TimeData(to_jtime(datetime.strptime(str((String[]) answer), "%H:%M").time()));
        } else if (datatype == XTypes.SELECT) {
            //TODO: convert choice
//            ans = SelectOneData(event["choices"][int(answer) - 1].to_sel());
        } else if (datatype == XTypes.MULTISELECT) {
            //TODO: convert multi
//            ans = SelectMultiData(to_vect(event["choices"][int(k) - 1].to_sel() for k in multians(answer)));
        } else if (datatype == XTypes.GEO) {
            //TODO convert geo
//            ans = GeoPointData(to_arr((float(x) for (x in multians(answer)), "d"));
        }

        if (ix == null) {
            //result = this.fec.answerQuestion( * ([ans]));  
        } else {
            //result = this.fec.answerQuestion( * ([ix, ans]));  
        }
        int result = 0;

        Status status = null;
        
        if (result == this.fec.ANSWER_REQUIRED_BUT_EMPTY) {

            status = new Status("error", "required");
        } else if (result == this.fec.ANSWER_CONSTRAINT_VIOLATED) {

//            q = this.fem.getQuestionPrompt( * ([] if ix == null{}}[ix]));
//            status = new Status("error", "constraint", q.getConstraintText());

        } else if (result == this.fec.ANSWER_OK) {

            status = new Status("success");
        }
        return status;
    }

    private boolean hasattr(String[] a, String __iter__) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String str(String[] a) {
        return "";
    }

    private void parseQuestion(Event event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void parseRepeatJuncture(Event event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private FormIndex parseIx(FormIndex ix) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object IntegerData(Integer valueOf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object LongData(Long valueOf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object DecimalData(Float valueOf) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object StringData(String str) {
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

    public void getQuestionChoices() {
    }

    public void parseStyleInfo() {
    }

    public void parseQuestion() {
    }

    public void parseRepeatJuncture() {
    }

    private FormIndex parseIx(int curIndex) {
        FormIndex fi = null;
        return fi;
    }

    private State dict(Params origParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean hasattr(Object a, String __iter__) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Safely cast an Object as a String
     *
     * @param a
     * @return
     */
    private String str(Object a) {
        String s = null;
        try {
            s = (String) a;
        } catch (ClassCastException cce) {

        }
        return s;
    }

    /**
     * To deal with strict typing
     *
     * @param a
     * @return
     */
    private String str(String s) {
        return s;
    }

}
