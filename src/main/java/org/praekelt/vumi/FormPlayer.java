package org.praekelt.vumi;

import com.google.gson.Gson;
import com.sun.jersey.core.header.MediaTypes;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import org.praekelt.tools.JedisFactory;
import org.praekelt.tools.RosaFactory;
import org.praekelt.xforms.SerializationException;

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

    /**
     * Creates a new instance of FormPlayer
     */
    public FormPlayer() {
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
        Set<String> set = JedisFactory.getInstance().getKeys("session*");

        Gson gson = new Gson();
        return gson.toJson(set);
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
        Set<String> set = JedisFactory.getInstance().getKeys("*.xml");

        Gson gson = new Gson();
        return gson.toJson(set);
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
    public String getSessionJson(String id) {
        JedisFactory.getInstance().get(id);
        return (new Gson()).toJson(id);
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
        RosaFactory xform = RosaFactory.getInstance();
        String id = xform.getUID();
        try {
            JedisFactory.getInstance().set(id, xform.serialize());
        } catch (SerializationException ex) {
            logger.log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
        return Response.ok(new Gson().toJson(id)).build();
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
    public String destroySession(String key) {
        JedisFactory.getInstance().delete(key);
        return "";
    }


}
