package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import java.util.Iterator;
import java.util.Map;
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
        
        private String uuid;
        private String xml;
    }
    
    public FormsResource(JedisClient jc) {
        super(jc);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        
        String id = this.createResource(payload);
        
        if (id != null) {
            return Response.status(Response.Status.CREATED).entity(
                String.format(
                    "{\"%s\": %d, \"%s\": %s, \"%s\": \"%s\"}",
                    "status", 201, "message", "success", "form", id
                )
            ).build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
            String.format(
                "{\"%s\": %d, \"%s\": %s, \"%s\": \"%s\"}",
                "status", 500, "message", "error creating form", "form", "{}"
            )
        ).build();
    }
    
    @Timed(name = "getSingle()")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") String formId) {
        
        String form = this.fetchResource(formId);
        
        if (form != null) {
            return Response.ok(
                String.format(
                    "{\"%s\": %d, \"%s\": \"%s\", \"%s\": %s}",
                    "status", 200, "message", "success", "form", form
                )
            ).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(
            String.format(
                "{\"%s\": %d, \"%s\": \"%s\", \"%s\": %s}",
                "status", 404, "message", "not found", "form", "{}"
            )
        ).build();
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
                "{ \"%s\": %d, \"%s\": \"%s\", \"%s\": %d, \"%s\": {",
                "status", 200, "message", "success", "count", keyCount, "forms"
            );
            
            while (i.hasNext()) {
                String current = i.next().toString();
                forms[key++] = String.format("\"%s\": %s", current, this.fetchResource(current));
            }
            response += this.implode(forms, ',') + "}}";
            return Response.ok().entity(response).build();
        }
        return Response.ok()
            .entity("{ \"status\": 200, \"count\": 0, \"forms\": {} }")
            .build();
    }
}
