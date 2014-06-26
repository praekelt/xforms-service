package org.praekelt.vumi;

import java.util.Iterator;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;
import org.praekelt.tools.JedisFactory;
import org.praekelt.tools.RosaFactory;

/**
 * REST Web Service
 *
 * @author victorgeere
 */
@Path("forms")
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
     *
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
    @Path("menu")
    @Produces("text/html")
    public String getHtml() {
        return "getHtml";
    }

    @GET
    @Path("sessions.json")
    @Produces("text/json")
    public String jsonGetSessions() {
        Set<String> set = JedisFactory.getInstance().getKeys("session*");

        String s = "{";
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String setElement = iterator.next();
            s += "{id:\""+setElement + "\"},";
        }
        s += "}";
        return s;
    }

    @GET
    @Path("sessions.html")
    @Produces("text/html")
    public String pkGetSessions() {
        Set<String> set = JedisFactory.getInstance().getKeys("session*");

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

    @GET
    @Path("forms.json")
    @Produces("text/json")
    public String jsonGetForms() {
        Set<String> set = JedisFactory.getInstance().getKeys("form*");

        String s = "{";
        Iterator<String> iterator = set.iterator();
        while (iterator.hasNext()) {
            String setElement = iterator.next();
            s += "{id:\""+setElement + "\"},";
        }
        s += "}";
        return s;
    }

    @GET
    @Path("forms.html")
    @Produces("text/json")
    public String htmlGetForms() {
        Set<String> set = JedisFactory.getInstance().getKeys("form*");

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

    @GET
    @Path("session.html/{id}")
    @Produces("text/html")
    public String getSession(String id) {
        JedisFactory.getInstance().get(id);
        return id;
    }

    @GET
    @Path("session.json/{id}")
    @Produces("text/json")
    public String getJsonSession(String id) {
        JedisFactory.getInstance().get(id);
        return id;
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

    @Path("sessions")
    @POST
    @Produces("text/html")
    public String newSessionHtml() {
        String id = "session-" + String.valueOf(Math.random());
        RosaFactory xform = RosaFactory.getInstance();
        JedisFactory.getInstance().set(id, xform.serialize());
        return id;
    }

    @Path("sessions")
    @POST
    @Produces("text/json")
    public String newSessionJson() {
        return newSessionHtml();
    }

    @Path("session/{id}")
    @DELETE
    @Produces("text/html")
    public String destroy_session(String key) {
        JedisFactory.getInstance().delete(key);
        return "";
    }


}
