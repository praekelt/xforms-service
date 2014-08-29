package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.setup.Environment;
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
import org.apache.commons.lang.StringUtils;
import org.praekelt.restforms.core.RestformsConfiguration;

/**
 *
 * @author Victor Geere
 * @author ant cosentino
 */
@Path("/forms")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FormsResource extends BaseResource {
    
    private static class FormsRepresentation {
        public FormsRepresentation() {}
    }
    
    public FormsResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        String id = this.generateUUID();
        jedisClient.set(id, payload);
        return Response.status(Response.Status.CREATED).build();
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
                jedisClient.get(formId)
            ))
            .build();
    }
    
    @Timed(name = "getAll()")
    @GET
    public Response getAll() {
        String response;
        Set<String> keys = jedisClient.getKeys();
        int keyCount = keys.size();
        
        if (keyCount > 0) {
            Iterator i = jedisClient.getKeys().iterator();
            int key = 0;
            String[] forms = new String[keyCount];
            response = String.format(
                "{ \"%s\": 200, \"%s\": \"%s\", \"%s\": %d, \"%s\": {",
                "status",
                "message",
                "success",
                "count",
                keyCount,
                "forms"
            );
            
            while (i.hasNext()) {
                String current = i.next().toString();
                String form = jedisClient.get(current);
                forms[key++] = "\"" + current + "\": " + form;
            }
            response += StringUtils.join(forms, ',') + "}}";
            return Response.status(Response.Status.OK).entity(response).build();
        }
        response = "{ \"status\": 200, \"forms\": {} }";
        return Response.status(Response.Status.OK).entity(response).build();
    }
}
