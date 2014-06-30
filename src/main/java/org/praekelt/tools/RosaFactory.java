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
import org.praekelt.xforms.Params;
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
    private final int stalenessWindow;
    private final Params origParams;

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
         'instance': self.output(),
         'init_lang': self.get_lang(),
         'cur_index': str(self.fem.getFormIndex()) if self.nav_mode != 'fao' else None,
         'seq_id': self.seq_id,
         });
         //# prune entries with null value, so that defaults will take effect when the session is re-created
         state = dict((k, v) for k, v in state.iteritems() if v is not null);
         */
        return state;
    }

    public void output() {
        /*
         if self.cur_event['type'] != 'form-complete':
         #warn that not at end of form
         pass

         instance_bytes = FormSerializer().serializeInstance(self.form.getInstance())
         return unicode(''.join(chr(b) for b in instance_bytes.tolist()), 'utf-8')        
         */
    }

    public void walk() {
        /*
         form_ix = FormIndex.createBeginningOfFormIndex()
         tree = []
         self._walk(form_ix, tree)
         return tree
         */
    }

    public void walk(int parentIx, int[] siblings) {
        /*
         def _walk(self, parent_ix, siblings):
         def step(ix, descend):
         next_ix = self.fem.incrementIndex(ix, descend)
         self.fem.setQuestionIndex(next_ix)  # needed to trigger events in form engine
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
         relevant = self.fem.isIndexRelevant(form_ix)

         if not relevant:
         form_ix = step(form_ix, False)
         continue

         evt = self.__parse_event(form_ix)
         evt['relevant'] = relevant
         if evt['type'] == 'sub-group':
         presentation_group = (evt['caption'] != None)
         if presentation_group:
         siblings.append(evt)
         evt['children'] = []
         form_ix = self._walk(form_ix, evt['children'] if presentation_group else siblings)
         elif evt['type'] == 'repeat-juncture':
         siblings.append(evt)
         evt['children'] = []
         for i in range(0, self.fem.getForm().getNumRepetitions(form_ix)):
         subevt = {
         'type': 'sub-group',
         'ix': self.fem.getForm().descendIntoRepeat(form_ix, i),
         'caption': evt['repetitions'][i],
         'repeatable': True,
         'children': [],
         }

         # kinda ghetto; we need to be able to track distinct repeat instances, even if their position
         # within the list of repetitions changes (such as by deleting a rep in the middle)
         # would be nice to have proper FormEntryAPI support for this
         java_uid = self.form.getInstance().resolveReference(subevt['ix'].getReference()).hashCode()
         subevt['uuid'] = hashlib.sha1(str(java_uid)).hexdigest()[:12]

         evt['children'].append(subevt)
         self._walk(subevt['ix'], subevt['children'])
         for key in ['repetitions', 'del-choice', 'del-header', 'done-choice']:
         del evt[key]
         form_ix = step(form_ix, True) # why True?
         else:
         siblings.append(evt)
         form_ix = step(form_ix, True) # why True?

         return form_ix
        
         */

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
        /*
         answer, _ix=None):
         ix = self.parse_ix(_ix)
         event = self.cur_event if ix is None else self.__parse_event(ix)

         if event['type'] != 'question':
         raise ValueError('not currently on a question')

         datatype = event['datatype']
         if datatype == 'unrecognized':
         # don't commit answers to unrecognized questions, since we
         # couldn't parse what was originally there. whereas for
         # _unsupported_ questions, we're parsing and re-committing the
         # answer verbatim
         return {'status': 'success'}

         def multians(a):
         if hasattr(a, '__iter__'):
         return a
         else:
         return str(a).split()

         if answer == None or str(answer).strip() == '' or answer == []:
         ans = None
         elif datatype == 'int':
         ans = IntegerData(int(answer))
         elif datatype == 'longint':
         ans = LongData(int(answer))
         elif datatype == 'float':
         ans = DecimalData(float(answer))
         elif datatype == 'str' or datatype == 'info':
         ans = StringData(str(answer))
         elif datatype == 'date':
         ans = DateData(to_jdate(datetime.strptime(str(answer), '%Y-%m-%d').date()))
         elif datatype == 'time':
         ans = TimeData(to_jtime(datetime.strptime(str(answer), '%H:%M').time()))
         elif datatype == 'select':
         ans = SelectOneData(event['choices'][int(answer) - 1].to_sel())
         elif datatype == 'multiselect':
         ans = SelectMultiData(to_vect(event['choices'][int(k) - 1].to_sel() for k in multians(answer)))
         elif datatype == 'geo':
         ans = GeoPointData(to_arr((float(x) for x in multians(answer)), 'd'))

         result = self.fec.answerQuestion(*([ans] if ix is None else [ix, ans]))
         if result == self.fec.ANSWER_REQUIRED_BUT_EMPTY:
         return {'status': 'error', 'type': 'required'}
         elif result == self.fec.ANSWER_CONSTRAINT_VIOLATED:
         q = self.fem.getQuestionPrompt(*([] if ix is None else [ix]))
         return {'status': 'error', 'type': 'constraint', 'reason': q.getConstraintText()}
         elif result == self.fec.ANSWER_OK:
         return {'status': 'success'}
         */

    }

    public void loadFile() {
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

    private State dict(Params origParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
