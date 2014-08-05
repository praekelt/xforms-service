package org.praekelt.tools;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.javarosa.core.api.State;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.xform.parse.XFormParser;
import org.praekelt.restforms.core.CCInstances;
import org.praekelt.restforms.core.Event;
import org.praekelt.restforms.core.KeyErrorException;
import org.praekelt.restforms.core.Lock;
import org.praekelt.restforms.core.Params;
import org.praekelt.restforms.core.Persistence;
import org.praekelt.restforms.core.SequencingException;
import org.praekelt.restforms.core.SerializationException;
import org.praekelt.restforms.core.Status;
import org.praekelt.restforms.core.ValueException;
import org.praekelt.restforms.core.XTypes;

/**
 * The object that will contain the xform state
 *
 * @author victorgeere
 */
public class RosaFactory implements Serializable {

    private static final Logger logger = Logger.getLogger(RosaFactory.class.getName());

    private boolean persist;
    private String uuid;
    private Lock lock;
    private String navMode;
    private int seqId;
    private final FormDef form;
    private FormEntryModel fem;
    private FormEntryController fec;
    private Date lastActivity;
    private final int stalenessWindow;
    private final Params origParams;
    private Event curEvent;
    private Object answer;
    private int datatype;
    private int ix;
    private Question q;
    private String nav_mode;
    private Object apiAuth;
    private HashMap sessionData;
    private int sessionId;

    private final GlobalStateMgr globalState = null;
    private String ev;
    private int formIx;
    private int repIx;
    private int instanceId;

    /**
     *
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
     * @throws org.praekelt.restforms.core.KeyErrorException
     */
    public RosaFactory(String navMode, int seqId, String xform,
            String instance, String extensions, HashMap sessionData,
            String apiAuth, String initLang, int curIndex, boolean persist, int stalenessWindow) throws KeyErrorException {

        this.uuid = RosaFactory.getUID();
        this.lock = Lock.getInstance();
        this.navMode = navMode;
        this.seqId = seqId;
        this.sessionData = sessionData;

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

        this.persist = persist; //params.get("persist", settings.PERSIST_SESSIONS);

        this.origParams = new Params(xform, navMode, sessionData, apiAuth, stalenessWindow);

        this.updateLastActivity();

    }

    public RosaFactory(String xform) {
        this.stalenessWindow = 3600;
        this.origParams = new Params(xform, navMode, sessionData, "", stalenessWindow);
        this.form = this.loadForm(xform, null);
    }

    /**
     * Get a unique-ish ID
     *
     * @return A unique id (not universally unique)
     */
    public static final String getUID() {
        String timestamp = String.valueOf(new Date().getTime());
        String uuid = "session-" + timestamp + "-" + String.valueOf(Math.abs(1 / Math.random()));
        return uuid;
    }

    /**
     * Get a RosaFactory object, deal with deserialization
     *
     * @return An instance of RosaFactory
     * @throws org.praekelt.restforms.core.KeyErrorException
     */
    public static RosaFactory getInstance() throws KeyErrorException {
        return new RosaFactory("", 0, "", "", "", new HashMap(), "", "", 0, true, 1);
    }

    /**
     *
     * @param xform
     * @return
     * @throws org.praekelt.restforms.core.KeyErrorException
     */
    public static RosaFactory getInstance(String xform) throws KeyErrorException {
//        return new RosaFactory("", 0, xform, null, "", new HashMap(), "", "", 0, true, 1);
        return new RosaFactory(xform);
    }

    /**
     * Serialize a form
     *
     * @param form The form to serialize
     * @return A serialized version of the form
     * @throws org.praekelt.restforms.core.SerializationException
     */
    public String serializeForm(FormDef form) throws SerializationException {
        String s = "";
        try {
            XFormSerializingVisitor fs = new XFormSerializingVisitor();
            byte[] ba = fs.serializeInstance(form.getInstance());
            s = new String(ba, "UTF-8");
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new SerializationException("Serialization failed.", ex);
        }
        return s;
    }

    /**
     *
     * @return A serialized (String) version of this.form
     * @throws org.praekelt.restforms.core.SerializationException
     */
    public String serializeForm() throws SerializationException {
        if (this.form == null) {
            throw new SerializationException("Form is null");
        }
        return this.serializeForm(this.form);
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
    public final FormDef loadForm(String xform, String instance, String extensions, HashMap sessionData, String apiAuth) {
        return this.loadForm(xform, instance);
    }

    /**
     * Instantiate a form
     *
     * @param xform
     * @param instance
     * @return
     */
    public final FormDef loadForm(String xform, String instance) {
        Reader xformReader = (Reader) new StringReader(xform);
        XFormParser xfp;
        xfp = new XFormParser(xformReader);
        FormDef xf = xfp.parse();
        if (instance != null) {
            StringReader sr = new StringReader(instance);
            XFormParser.loadXmlInstance(xf, sr);
        }
        this.formInitialize(instance, new CCInstances(this.sessionData, this.apiAuth), xf);
        return xf;
    }

    /**
     *
     * @param instance
     * @param cci
     * @param form
     */
    private void formInitialize(String instance, CCInstances cci, FormDef form) {
        form.initialize(false);
    }

    /**
     *
     * @return @throws org.praekelt.restforms.core.SequencingException
     */
    public RosaFactory enter() throws SequencingException {
        if (this.navMode.equalsIgnoreCase("fao")) {
            this.lock.acquire(RosaFactory.getUID());
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
            Persistence.persist(this);
        }
        this.lock.release(RosaFactory.getUID());
    }

    /**
     * Set when the last activity took place
     */
    public final void updateLastActivity() {
        this.lastActivity = new Date();//time.time();
    }

    /**
     *
     * @return
     */
    public String getLang() {
        return "";
    }

    /**
     *
     * @return
     */
    public State sessionState() {
        Params state = this.origParams;

        state.update(this.output(),
                this.getLang(),
                (!this.nav_mode.equalsIgnoreCase("foa")) ? str(this.fem.getFormIndex()) : null,
                this.seqId
        );
        //# prune entries with null value, so that defaults will take effect when the session is re-created
        // might not be necessary in Java
//         state = dict((k, v) for k, v in state.iteritems() if v is not null);

        return state;
    }

    /**
     *
     * @return
     */
    public String output() {
        return "";
    }

    public FormIndex step(FormIndex form_ix, boolean b) {
        return form_ix;
    }

    /**
     *
     * @param form_ix
     * @return
     */
    public boolean ixInScope(FormIndex form_ix) {

        return false;

    }

    /**
     * Parse the current event
     *
     * @return
     */
    private Event parseCurrentEvent() throws KeyErrorException {
        this.curEvent = this.parseEvent(this.fem.getFormIndex());
        return this.curEvent;
    }

    /**
     *
     * @param formIx
     * @return
     * @throws org.praekelt.restforms.core.KeyErrorException
     */
    public Event parseEvent(FormIndex formIx) throws KeyErrorException {
        Event event = new Event();
        event.setFi(formIx);

        int status = this.fem.getEvent(formIx);

        if (status == FormEntryController.EVENT_BEGINNING_OF_FORM) {
            event.setType("form-start");
        } else if (status == FormEntryController.EVENT_END_OF_FORM) {
            event.setType("form-complete");
        } else if (status == FormEntryController.EVENT_QUESTION) {
            event.setType("question");
            this.parseQuestion(event);
        } else if (status == FormEntryController.EVENT_REPEAT_JUNCTURE) {
            event.setType("repeat-juncture");
            FormIndex[] fi = null;
            this.parseRepeatJuncture(event);
        } else {
            event.setType("sub-group");
            FormEntryCaption prompt = this.fem.getCaptionPrompt(formIx);
            event.setCaption(prompt.getLongText());
            event.setCaptionAudio(prompt.getAudioText());
            event.setCaptionImage(prompt.getImageText());
//            TEXT_FORM_VIDEO does not exist
//            FormEntryPrompt.TEXT_FORM_VIDEO
//            event.captionVideo(prompt.getSpecialFormQuestionText(FormEntryPrompt.TEXT_FORM_VIDEO));
            if (status == FormEntryController.EVENT_GROUP) {

                event.setRepeatable(false);
            } else if (status == FormEntryController.EVENT_REPEAT) {

                event.setRepeatable(true);
                event.setExists(true);
            } else if (status == FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                event.setRepeatable(true);
                event.setExists(false);
            }
        }
        return event;
    }

    /**
     *
     * @return @throws org.praekelt.restforms.core.KeyErrorException
     */
    public Event nextEvent() throws KeyErrorException {
        this.fec.stepToNextEvent();
        return this.parseCurrentEvent();
    }

    /**
     *
     * @return @throws org.praekelt.restforms.core.KeyErrorException
     */
    public Event backEvent() throws KeyErrorException {
        this.fec.stepToPreviousEvent();
        return this.parseCurrentEvent();
    }

    /**
     *
     *
     * @param answer
     * @param ix
     * @return
     * @throws org.praekelt.restforms.core.ValueException
     * @throws org.praekelt.restforms.core.KeyErrorException
     */
    public Status answerQuestion(Object answer, FormIndex ix) throws ValueException, KeyErrorException {
        ix = this.parseIx(ix);
        Event event = this.curEvent;

        if (ix != null) {
            this.parseEvent(ix);
        }

        if (0 != event.getType().compareToIgnoreCase("question")) {
            throw new ValueException("not currently on a question");
        }

        int dtype = event.getDataType();
        if (dtype == Event.UNRECOGNISED) {

        }

//         # don"t commit answers to unrecognized questions, since we
//         # couldn"t parse what was originally there. whereas for
//         # _unsupported_ questions, we"re parsing and re-committing the
//         # answer verbatim
        return new Status("success");
    }

    private Object toJDate(DateTime dt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object toJTime(DateTime dt) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        if (answer == null || str(answer).trim().equalsIgnoreCase("") /**
                 * || answer == []
                 */
                ) {
            ans = null;
        } else if (datatype == XTypes.INT) {
            ans = IntegerData(answer.toString());
        } else if (datatype == XTypes.LONG) {
            ans = LongData(answer.toString());
        } else if (datatype == XTypes.FLOAT) {
            ans = DecimalData(answer.toString());
        } else if (datatype == XTypes.STRING | datatype == XTypes.INFO) {
            ans = StringData(str((String[]) answer));
        } else if (datatype == XTypes.DATE) {
            ans = DateData(this.toJDate(DateTime.strptime(str(answer), "%Y-%m-%d").date()));
        } else if (datatype == XTypes.TIME) {
            ans = TimeData(this.toJTime(DateTime.strptime(str((String[]) answer), "%H:%M").time()));
        } else if (datatype == XTypes.SELECT) {
//            ans = SelectOneData(event["choices"][int(answer) - 1].toSel());
        } else if (datatype == XTypes.MULTISELECT) {
            //TODO: convert multi
//            ans = SelectMultiData(to_vect(event["choices"][int(k) - 1].to_sel() for k in multians(answer)));
        } else if (datatype == XTypes.GEO) {
            //TODO convert geo
//            ans = GeoPointData(to_arr((float(x) for (x in multians(answer)), "d"));
        }

        if (ix == 0) {
            //result = this.fec.answerQuestion( * ([ans]));  
        } else {
            //result = this.fec.answerQuestion( * ([ix, ans]));  
        }
        int result = 0;

        Status status = null;

        if (result == FormEntryController.ANSWER_REQUIRED_BUT_EMPTY) {

            status = new Status("error", "required");
        } else if (result == FormEntryController.ANSWER_CONSTRAINT_VIOLATED) {

//            q = this.fem.getQuestionPrompt( * ([] if ix == null{}}[ix]));
//            status = new Status("error", "constraint", q.getConstraintText());
        } else if (result == FormEntryController.ANSWER_OK) {

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

    /**
     *
     * @param event
     */
    private void parseQuestion(Event event) throws KeyErrorException {
        this.fem.getQuestionPrompt(event.getByIx("ix"));

        event.setByIx("caption", q.getLongText());
        event.setByIx("caption_audio", q.getAudioText());
        event.setByIx("caption_image", q.getImageText());
        //this version javarosa does not have FormEntryPrompt.TEXT_FORM_VIDEO
        //event("caption_video", q.getSpecialFormQuestionText(FormEntryPrompt.TEXT_FORM_VIDEO));
        event.setByIx("help", q.getHelpText());
        event.setByIx("style", this.parseStyleInfo(q.getAppearanceHint()));
        event.setByIx("binding", q.getQuestion().getBind().getReference().toString());

        if (q.getControlType() == Constants.CONTROL_TRIGGER) {
            event.setByIx("datatype", "info");
        } else {
            try {
                Gson gson = new Gson();

                String json = "{"
                        + Constants.DATATYPE_NULL + ": \"str\","
                        + Constants.DATATYPE_TEXT + ": \"str\","
                        + Constants.DATATYPE_INTEGER + ": \"int\","
                        + Constants.DATATYPE_LONG + ": \"longint\", "
                        + Constants.DATATYPE_DECIMAL + ": \"float\","
                        + Constants.DATATYPE_DATE + ": \"date\","
                        + Constants.DATATYPE_TIME + ": \"time\","
                        + Constants.DATATYPE_CHOICE + ": \"select\","
                        + Constants.DATATYPE_CHOICE_LIST + ": \"multiselect\","
                        + Constants.DATATYPE_GEOPOINT + ": \"geo\","
                        + //# not supported yet
                        Constants.DATATYPE_DATE_TIME + ": \"datetime\","
                        + Constants.DATATYPE_BARCODE + ": \"barcode\","
                        + Constants.DATATYPE_BINARY + ": \"binary\","
                        + "}";

                Object obj[] = gson.fromJson(json, Object[].class);

                event.setByIx("datatype", obj[q.getDataType()]);

            } catch (KeyErrorException kee) {
                event.setByIx("datatype", "unrecognized");
            }
            if (event.getDataType() == Event.SELECT || event.getDataType() == Event.MULTISELECT) {
                event.setByIx("choices", this.getQuestionChoices(q));
            }
            event.setByIx("required", q.isRequired());

            String value = q.getAnswerValue();
            if (value == null) {
                event.setByIx("answer", null);
            } else if (event.getDataType() == Event.INT
                    || event.getDataType() == Event.FLOAT
                    || event.getDataType() == Event.STR
                    || event.getDataType() == Event.LONGINT) {
//                event.setByIx("answer", value.getValue());
            } else if (event.getDataType() == Event.DATE) {
//                event.setByIx("answer") = this.toPdate(value.getValue());
            } else if (event.getDataType() == Event.TIME) {
//                event.setByIx("answer") = this.toPdate(value.getValue());
            } else if (event.getDataType() == Event.SELECT) {
//                event.setByIx("answer") = choice(q, selection = value.getValue()).ordinal()
            } else if (event.getDataType() == Event.MULTISELECT) {
//                event.setByIx("answer") = [choice(q, selection = sel).ordinal() for sel in  value.getValue() {}];
            } else if (event.getDataType() == Event.GEO) {
//                event.setByIx("answer") = list(value.getValue())[:2];
            }
        }
    }

    private void parseRepeatJuncture(Event event) {
        FormEntryCaption r = this.fem.getCaptionPrompt(event.getByIx("ix"));
        FormEntryCaption.RepeatOptions ro = r.getRepeatOptions();
        event.getSetByIx("main-header", ro.header);
        event.getSetByIx("repetitions", list(r.getRepetitionsText()));

        event.getSetByIx("add-choice", ro.add);
        event.getSetByIx("del-choice", ro.delete);
        event.getSetByIx("del-header", ro.delete_header);
        event.getSetByIx("done-choice", ro.done);
    }

    private FormIndex parseIx(int curIndex) {
        FormIndex fi = null;
        return fi;
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

    private FormIndex parseIx(String sIx) {
        return indexFromStr(sIx, this.form);
    }

    private FormIndex parseIx(FormIndex fix) {
        return fix;
    }

    private int IntegerData(String sInt) {
        return Integer.valueOf(sInt);
    }

    private Long LongData(String sLong) {
        return Long.valueOf(sLong);
    }

    private Float DecimalData(String sDeci) {
        return Float.valueOf(sDeci);
    }

    /**
     * @param spec
     * @param kwargs
     * @return
     */
    public HashMap getLoader(boolean spec, HashMap kwargs) {
        if (!spec) {
            return null;
        }
        Object type, val = spec;
        HashMap hm = new HashMap();
        hm.put("uid", this.loadFile(val));
        hm.put("raw", val);
        hm.put("url", queryFactory(kwargs.get("api_auth"), "raw"));
        return hm;
    }

    /**
     *
     * @param xfsess
     * @return
     */
    public Map<String, String> initContext(XFormSession xfsess) {
        xfsess = globalState.getSession(this.sessionId);
        Map<String, String> evs = new HashMap<String, String>();
        evs.put("event", ev);
        return evs;

        /*
         """return the "extra" response context needed when initializing a session"""
         return {
         "title": xfsess.form_title(),
         "langs": xfsess.get_locales(),
         }
         */
    }

    public String openForm(boolean formSpec, boolean instSpec, HashMap kwargs) {
        Object xformXml;
        Object instanceXml;
        try {
            xformXml = this.getLoader(formSpec, kwargs);
            instanceXml = this.getLoader(instSpec, kwargs);
        } catch (Exception e) {
            return e.getMessage();

        }

        XFormSession xfsess = new XFormSession(xformXml, instanceXml, kwargs);
        this.globalState.newSession(xfsess);
        Extra extra = new Extra("session_id", xfsess.uid);
        extra.update(this.initContext(xfsess));
        return xfsess.response(extra);
    }

    public Map<String, String> editRepeat() {
        XFormSession xfsess = globalState.getSession(this.sessionId);
        String event = xfsess.descendRepeat(ix);
        Map<String, String> evs = new HashMap<String, String>();
        evs.put("event", event);
        return evs;
    }

    public Object newRepeat() {
        XFormSession xfsess = this.globalState.getSession(sessionId);
        ev = xfsess.descendRepeat(this.formIx);
        return xfsess.response(null, ev);
    }

    public Object deleteRepeat() {
        XFormSession xfsess = globalState.getSession(sessionId);
        ev = xfsess.deleteRepeat(this.repIx, this.formIx);
        return xfsess.response(null, ev);
    }

    public void newRepitition() {
        XFormSession xfsess = globalState.getSession(sessionId);
        xfsess.newRepetition();
        Map<String, String> evs = new HashMap<String, String>();
        evs.put("event", nextEvent(xfsess));
    }

    public void skipNext() {
        XFormSession xfsess = globalState.getSession(sessionId);
        Map<String, String> evs = new HashMap<String, String>();
        evs.put("event", nextEvent(xfsess));
    }

    public void goBack() {
        XFormSession xfsess = globalState.getSession(sessionId);
//         (atStart, event) = prev_event(xfsess);
        Map<String, String> evs = new HashMap<String, String>();
        evs.put("event", nextEvent(xfsess));
    }

    public Object submitForm() throws Throwable {
        XFormSession xfsess = globalState.getSession(sessionId);
        boolean errors = false;
        boolean prevalidated = false;
        Object resp = null;
//         errors = dict(filter(lambda resp: resp[1]["status"] != "success",
//         ((_ix, xfsess.answer_question(answer, _ix)) for _ix, answer in answers.iteritems())))

        if (errors || !prevalidated) {
            //resp = {"status": "validation-error", "errors": errors};
        } else {
            resp = formCompletion(xfsess);
            //resp["status"] = "success";
        }

        return xfsess.response(resp, true);
    }

    public Object setLocale(String lang) {
        XFormSession xfsess = globalState.getSession(sessionId);
        ev = xfsess.setLocale(lang);
        return xfsess.response(null, ev);
    }

    /**
     *
     * @return
     */
    public Object currentQuestion() {
        XFormSession xfsess = globalState.getSession(sessionId);
        Extra extra = Extra.getInstance("lang", xfsess.getLang());
        extra.update(initContext(xfsess));
        return xfsess.response(extra, xfsess.curEvent);
    }

    public Object heartbeat() {
        Object xfss = globalState.getSession(sessionId);
        return xfss;
    }

    public void prevEvent() {
//         atStart, ev = False, xfsess.back_event();
//         if (ev["type"] == "form-start"){
//            atStart, ev = True, xfsess.next_event();
//            }
//        return atStart, ev;
    }

    public Tuple saveForm(XFormSession xfsess) throws Throwable {
        xfsess.fin();
        Object xml = xfsess.output();
        if (persist) {
            this.instanceId = globalState.saveInstance(xml);
        } else {
            this.instanceId = 0;
        }
        return new Tuple(instanceId, xml);
    }

    public State formCompletion(XFormSession xfsess) throws Throwable {
        return dict(zip(Tuple.make("save-id", "output"), saveForm(xfsess)));
    }

    /**
     *
     * @return
     */
    public Response purge() {
        Response resp = globalState.purge();

        resp.update(PMap.make("status", "ok"));
        return resp;
    }

    public void printTree(Object tree) {
        PPrint pp = PPrint.PrettyPrinter(2);
        try {
            pp.pprint(tree);
        } catch (UnicodeEncodeError uee) {
            System.err.println("sorry, can\"t pretty-print unicode");
        }
    }

    public String[] parseStyleInfo(String rawstyle) {
        Dict dInfo = new Dict();

        if (rawstyle != null) {
            dInfo.put("raw", rawstyle);
            try {
                dInfo.update();
//            info.update([[p.strip() for p in f.split(":")][:2] for f in rawstyle.split(";") if f.strip()])
            } catch (ValueError ve) {
                //pass
            }
            return dInfo.toStringArray();

        }
        return null;
    }

    public int[] walk() {
        FormIndex formIx = FormIndex.createBeginningOfFormIndex();
        int[] tree = null;
        this.walk(formIx, tree);
        return tree;
    }

    public void walk(FormIndex parentIx, int[] siblings) {
        /*
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
         evt["relevant"] = relevant
         if evt["type"] == "sub-group":
         presentation_group = (evt["caption"] != None)
         if presentation_group:
         siblings.append(evt)
         evt["children"] = []
         form_ix = self._walk(form_ix, evt["children"] if presentation_group else siblings)
         elif evt["type"] == "repeat-juncture":
         siblings.append(evt)
         evt["children"] = []
         for i in range(0, self.fem.getForm().getNumRepetitions(form_ix)):
         subevt = {
         "type": "sub-group",
         "ix": self.fem.getForm().descendIntoRepeat(form_ix, i),
         "caption": evt["repetitions"][i],
         "repeatable": True,
         "children": [],
         }

         # kinda ghetto; we need to be able to track distinct repeat instances, even if their position
         # within the list of repetitions changes (such as by deleting a rep in the middle)
         # would be nice to have proper FormEntryAPI support for this
         java_uid = self.form.getInstance().resolveReference(subevt["ix"].getReference()).hashCode()
         subevt["uuid"] = hashlib.sha1(str(java_uid)).hexdigest()[:12]

         evt["children"].append(subevt)
         self._walk(subevt["ix"], subevt["children"])
         for key in ["repetitions", "del-choice", "del-header", "done-choice"]:
         del evt[key]
         form_ix = step(form_ix, True) # why True?
         else:
         siblings.append(evt)
         form_ix = step(form_ix, True) # why True?

         return form_ix
         */
    }

    private String getQuestionChoices(Question q) {
        //return [choice(q, ch) for ch in q.getSelectChoices()]
        return "";
    }

    private String list(Vector<String> repetitionsText) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private FormIndex indexFromStr(String sIx, FormDef form) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object StringData(String str) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private State dict(Params... origParams) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private boolean hasattr(Object a, String __iter__) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object loadFile(Object val) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object queryFactory(Object get, String raw) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String nextEvent(XFormSession xfsess) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object DateData(Object toJDate) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Object TimeData(Object toJTime) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private Params[] zip(Tuple make, Tuple saveForm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
