package org.praekelt.restforms.resources;

import com.google.gson.Gson;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.praekelt.restforms.core.KeyErrorException;

import org.praekelt.restforms.core.SerializationException;
import org.praekelt.tools.JedisClient;
import org.praekelt.tools.RosaFactory;

/**
 * REST Web Service
 *
 * @author victorgeere
 */
@Path("/forms")
public class FormPlayer {

    Logger logger;
    
    @Context
    private UriInfo context;

	private JedisClient jedis;

    /**
     * Creates a new instance of FormPlayer
     */
    public FormPlayer(JedisClient jedis) {
        this.jedis = jedis;
		logger = Logger.getLogger(FormPlayer.class.getName());
    }

    /**
     * Checks to see that the FormPlayer is up and running
     * 
     * @return A JSON encoded "alive" String
     */
    @GET
    @Path("status")
    @Produces(MediaType.APPLICATION_JSON)
    public String getStatus() {
        return new Gson().toJson("alive");
    }

    /**
     * Retrieves representation of an instance of org.praekelt.FormPlayer. 
     *
     * @return a list of sessions in JSON format. 
     * 
     * @deprecated this will fall away when the client is up and running but is used for debugging at the moment
     */

    @GET
    @Path("sessions.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonGetSessions() {
        Set<String> set = this.jedis.getKeys("session*");

        Gson gson = new Gson();
        return gson.toJson(set);
    }

    /**
     * Create a new session
     * 
     * @return the new session id in JSON
     */
    @Path("sessions")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response newSessionJson() {
        String id = RosaFactory.getUID();
        this.jedis.set(id, "");
        return Response.ok(new Gson().toJson(id)).build();
    }

    /**
     * Load an existing session
     * 
     * @param id
     * @return the loaded id in JSON
     */
    @GET
    @Path("session.json/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public String getSessionJson(@PathParam("id") String id) {
        this.jedis.get(id);
        return (new Gson()).toJson(id);
    }

    /**
     * Delete a key
     * 
     * @param key
     * 
     * @return empty String
     */
    @Path("session.json/{id}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response destroySession(String key) {
        this.jedis.delete(key);
        return Response.ok("{}").build();
    }

    /**
     * Get all forms
     * 
     * @return A list of forms in JSON
     */
    @GET
    @Path("forms.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String jsonGetForms() {
        Set<String> set = this.jedis.getKeys("*.xml");
        Gson gson = new Gson();
        return gson.toJson(set);
    }

    /**
     * Delete a key
     * 
     * @param formName
     * 
     * @return empty String
     */
    @Path("form.json/{formName}")
    @DELETE
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteForm(@PathParam("formName") String formName) {
        this.jedis.delete(formName);
        return Response.ok("{}").build();
    }

    /**
     * Load a form into a session
     * 
     * @param formName
     * @return 
     */
    @GET
    @Path("/form.json/{formName}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response jsonGetForm(@PathParam("formName") String formName) throws KeyErrorException {
        String xformStr = this.jedis.get(formName);
        RosaFactory xform = RosaFactory.getInstance(xformStr);
        String id = xform.getUID();
        try {
            this.jedis.set(id, xform.serializeForm());
        } catch (SerializationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
        return Response.ok(new Gson().toJson(id)).build();
    }
}
