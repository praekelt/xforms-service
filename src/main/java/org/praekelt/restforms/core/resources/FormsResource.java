package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import java.util.Iterator;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
 */
@Path("/forms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FormsResource extends BaseResource {
    
    private static class FormsRepresentation {
        public FormsRepresentation() {}
    }
    
    public FormsResource(JedisClient jc) {
        super(jc);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        String id = this.createResource(payload);
        String response = String.format("{\"%s\": %d, \"%s\": %s, \"%s\": \"%s\"}",
            "status", 201, "message", "success", "form", id
        );
        return Response.status(Response.Status.CREATED).entity(response).build();
    }
    
    @Timed(name = "getSingle()")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") String formId) {
        return Response
            .status(Response.Status.OK)
            .entity(String.format(
                "{\"%s\": 200, \"%s\": \"%s\", \"%s\": %s}",
                "status",
                "message",
                "success",
                "form",
                this.fetchResource(formId)
            ))
            .build();
    }
    
    @Timed(name = "getAll()")
    @GET
    public Response getAll() {
        String response;
        Set<String> keys = jedis.getKeys();
        int keyCount = keys.size();
        
        if (keyCount > 0) {
            Iterator i = jedis.getKeys().iterator();
            int key = 0;
            String[] forms = new String[keyCount];
            response = String.format(
                "{ \"%s\": 200, \"%s\": \"%s\", \"%s\": %d, \"%s\": {",
                "status", "message", "success", "count", keyCount, "forms"
            );
            
            while (i.hasNext()) {
                String current = i.next().toString();
                String form = this.fetchResource(current);
                forms[key++] = String.format("\"%s\": %s", current, form);
            }
            response += this.implode(forms, ',') + "}}";
            return Response.ok().entity(response).build();
        }
        return Response.ok()
            .entity("{ \"status\": 200, \"count\": 0, \"forms\": {} }")
            .build();
    }
}
