package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.setup.Environment;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
        
        //an important note:
        //
        //any properties of this class should have
        //a SerializedName annotation, even if it is
        //identical to the property name. this will
        //allow us to safely rename our properties
        //once we have decided upon a strict document format.
    }
    
    public FormsResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        
        //example:
        
        //use the payload to construct a pojo representing the json document provided in the http request
        FormsRepresentation fr = (FormsRepresentation) this.fromJson(payload, FormsRepresentation.class);
        
        //clean up/validate any data contained in the pojo
        // if (dataIsDirty(ar)) {
        //  reject
        // } else {
        //  store in db
        // }
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "getSingle()")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") String formId) {
        
        //example:
        
        //assuming jedis simply stores a json document...
        return Response
                .status(Response.Status.OK)
                .entity(jedisClient.get(formId))
                .build();
    }
    
    @Timed(name = "getAll()")
    @GET
    public Response getAll() {
        
        String response;
        Set<String> keys = jedisClient.getKeys();
        
        if (keys.size() > 0) {
            
            response = "";
            
            for (String key : keys) {
                response += jedisClient.get(key);
            }
            return Response.status(Response.Status.OK).entity(response).build();
        }
        response = "{ \"status\": 200, \"forms\": [] }";
        return Response.status(Response.Status.OK).entity(response).build();
    }
}
