package org.praekelt.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.javarosa.model.xform.XFormSerializingVisitor;

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
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.xform.parse.XFormParser;

/**
 * The object that will contain the xform state
 *
 * @author victorgeere
 */
public class RosaFactory implements Serializable {

    /**
     * Get a RosaFactory object, deal with deserialization 
     * 
     * @return 
     */
    public static RosaFactory getInstance() {
        return new RosaFactory();
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

    public void enter() {

    }

    public void exit() {

    }

    public void update_last_activity() {

    }

    public void session_state() {

    }

    public void output() {
    }

    public void walk() {
    }

    public void ix_in_scope() {
    }

    public void parse_current_event() {
    }

    public void parse_event() {
    }

    public void get_question_choices() {
    }

    public void parse_style_info() {
    }

    public void parse_question() {
    }

    public void parse_repeat_juncture() {
    }

    public void next_event() {
    }

    public void back_event() {
    }

    public void answer_question() {
    }

    public void load_file() {
    }

    public void get_loader() {
    }

    public void init_context() {
    }

    public void open_form() {
    }

    public void edit_repeat() {
    }

    public void new_repeat() {
    }

    public void delete_repeat() {
    }

    public void new_repitition() {
    }

    public void skip_next() {
    }

    public void go_back() {
    }

    public void submit_form() {
    }

    public void set_locale() {
    }

    public void current_question() {
    }

    public void heartbeat() {
    }

    public void prev_event() {
    }

    public void save_form() {
    }

    public void form_completion() {
    }

    public void purge() {
    }

}
