package org.praekelt.restforms.resources;

import java.util.Iterator;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.praekelt.tools.JedisClient;
import org.praekelt.tools.Props;

/**
 *
 * REST Web Service
 *
 * The REST endpoint is made up of
 * /context/url-pattern/resourceClassPath/methodPath
 *
 * context is set in context.xml at /context[path] url-pattern is set in web.xml
 * at /web-app/servlet-mapping/url-pattern resourceClassPath is set with the
 *
 * @Path annotation in the class (this file) methodPath is set with the @Path
 * annotation at the method
 *
 * @author Victor
 */
@Path("/rest")
public class Forms {

    @Context
    private UriInfo context;
    private JedisClient jedis;

    /**
     * Creates a new instance of Forms
     */
    public Forms(JedisClient jedis) {
        this.jedis = jedis;
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
    @Path("status")
    @Produces("text/html")
    public String getStatus() {
        return "alive";
    }
    
    /**
     *
     *
     * @return
     */
    @GET
    @Path("forms")
    @Produces("text/html")
    public String getHtmlList() {

        Set<String> set = this.jedis.getKeys("*.xml");

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

    /**
     * Get a specific empty form
     *
     * @param id
     * @return
     */
    @GET
    @Path("form/{id}")
    @Produces("text/xml")
    public String getForm(@PathParam("id") String id) {
        String form = this.jedis.get(id);
        return form;
    }

    /**
     * Delete a from from Redis
     *
     * @param id
     * @return
     */
    @DELETE
    @Path("form/{id}")
    public String deleteForm(@PathParam("id") String id) {
        this.jedis.delete(id);
        return "done";
    }

    /**
     * Delete a from from Redis. Strictly speaking not a REST method, but
     * included for convenience.
     *
     * @param id
     * @return
     */
    @GET
    @Path("delete/{id}")
    @Produces("text/plain")
    public String deleteResultForm(@PathParam("id") String id) {
        this.jedis.delete(id);
        return "done";
    }

    /**
     * Return all items as xml
     *
     * @return
     */
    @GET
    @Path("formList")
    @Produces("text/xml")
    public String getXmlList() {
        return getXmlList("*.xml");
    }

    /**
     * Return all items as xml
     *
     * @return
     */
    @GET
    @Path("results")
    @Produces("text/html")
    public String getHtmlResults() {
        return getHtmlList("result:*", "result/");
    }

    /**
     * Return all items as xml
     *
     * @return
     */
    @GET
    @Path("completed")
    @Produces("text/xml")
    public String getODKXmlResults() {
        return getXmlList("result:*");
    }

    /**
     * Get a specific result
     *
     * @param id
     * @return
     */
    @GET
    @Path("result/{id}")
    @Produces("text/plain")
    public String getResult(@PathParam("id") String id) {
        String result = this.jedis.get(id);
        return result;
    }

    @POST
    @Path("submission")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_XML)
    public Response postResults(@QueryParam("deviceID") String name, String content) throws InterruptedException {
        name = "result:" + name + 1 / Math.random();
        try {
            this.jedis.set(name, content);
        } catch (NullPointerException npe) {
            Response.status(500).build();
        }
        return Response.status(201).build();
    }

    @HEAD
    @Path("submission")
    @Produces(MediaType.TEXT_PLAIN)
    public Response postHead() {
        return Response.status(204).build();
    }

    /**
     * Get list of available forms as xml
     *
     * @return A list of forms. formList has to have a capital L because odk
     * requires it
     */
    private String getXmlList(String filter) {
        Props p = new Props();
        Set<String> set = this.jedis.getKeys(filter);
        String s = "<forms>";
        if (set != null) {
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String setElement = iterator.next();
                s += "<form url=\"" + p.get("server.hosturl") + "/forms/rest/form/" + setElement + "\">" + setElement + "</form>";
            }
        }
        s += "</forms>";
        return s;
    }

    /**
     *
     * Return Redis query results as html
     *
     * @param filter a redis filter pattern
     * @return an html String
     */
    private String getHtmlList(String filter, String prefix) {
        Set<String> set = this.jedis.getKeys(filter);
        String s = getHeader();
        if (set != null) {
            s += "<ul>";
            Iterator<String> iterator = set.iterator();
            while (iterator.hasNext()) {
                String setElement = iterator.next();
                s += "<li><a href=\"" + prefix + "" + setElement + "\">" + setElement + "</a></li>";
            }
            s += "</ul>";
        }
        s += getFooter();
        return s;
    }

}
