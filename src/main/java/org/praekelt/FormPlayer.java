package org.praekelt;

import java.util.Iterator;
import java.util.Set;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.PathParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.Produces;
import org.praekelt.tools.JedisFactory;

/*
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IntegerData;
//import org.javarosa.core.model.data.LongData;
import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.DateData;
import org.javarosa.core.model.data.TimeData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.util.UnregisteredLocaleException;
import org.javarosa.model.xform.XFormSerializingVisitor;
*/

/**
 * REST Web Service
 *
 * @author victorgeere
 */
@Path("/vumi")
public class FormPlayer {

    @Context
    private UriInfo context;

    /**
     * Creates a new instance of FormPlayer
     */
    public FormPlayer() {
    }

    /**
     * Retrieves representation of an instance of org.praekelt.FormPlayer
     * @return an instance of java.lang.String
     */
    /*
    @GET
    @Produces("application/xml")
    public String getXml() {
        return "getXml";
    }
    */
    @GET
    @Produces("text/html")
    public String getHtml() {
        return "getHtml";
    }
    
    /**
     * PUT method for updating or creating an instance of FormPlayer
     * @param content representation for the resource
     * @return an HTTP response with content of the updated or created resource.
     */
    @PUT
    @Consumes("application/xml")
        public void putXml(String content) {
    }
        
    @GET
    @Path("session/{id}")
    public String getSession(String id) {
        JedisFactory.getInstance().get(id);
        return "getHtmlList";
    }

    /**
     *
     * @return
     */
    private String getHeader() {
        String s = "<!DOCTYPE html><html>"
                + "<head>        "
                + "<title>XForms</title>\n"
                + "        <meta charset=\"windows-1252\">\n"
                + "        <META HTTP-EQUIV=\"CACHE-CONTROL\" CONTENT=\"NO-CACHE\">"
                + "        <META HTTP-EQUIV=\"PRAGMA\" CONTENT=\"NO-CACHE\">"
                + "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n"
                + "        <link href=\"http://fonts.googleapis.com/css?family=Lato:300,400,700\" rel=\"stylesheet\" type=\"text/css\">\n"
                + "        <link rel=\"stylesheet\" type=\"text/css\" href=\"/xforms.css\">"
                + "</head>"
                + "<body>        "
                + "<div class=\"wrapper\">\n"
                + "        <h1>XForms</h1>\n"
                + "        <div class=\"content\">";
        return s;
    }

    /**
     *
     * @return
     */
    private String getFooter() {
        String s = "</body></html>";
        return s;
    }
    
    @GET
    @Path("sessions")
    public String getSessions() {
        Set<String> set = JedisFactory.getInstance().getKeys("*.xml");

        String s = getHeader();
        s += "<ul>";
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String setElement = iterator.next();
            s += "<li><a href=\"/forms/rest/form/" + setElement + "\">" + setElement + "</a></li>";
        }
        s += "</ul>";
        s += getFooter();
        return s;
    }

    public void init() {
    }
    
    public void new_session() {
    }
    
    public void get_session() {
    }
    
    public void destroy_session() {
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
