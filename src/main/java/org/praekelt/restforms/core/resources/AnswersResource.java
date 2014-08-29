package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.setup.Environment;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.praekelt.restforms.core.RestformsConfiguration;

/**
 *
 * @author ant cosentino
 */
@Path("/answers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnswersResource extends BaseResource implements BaseResource.Representable {
    
    private static class AnswersRepresentation {
        public AnswersRepresentation() {}
        
        //an important note:
        //
        //any properties of this class should have
        //a SerializedName annotation, even if it is
        //identical to the property name. this will
        //allow us to rename our properties even once
        //we have decided upon a strict document format.
    }
    
    public AnswersResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Override
    public String to(Object base) {
        return gson.toJson(base, AnswersRepresentation.class);
    }

    @Override
    public Object from(String json) {
        return gson.fromJson(json, AnswersRepresentation.class);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "update(id)")
    @PUT
    @Path("{formId}")
    public Response update(@PathParam("formId") int formId, String payload) {
        return Response.status(Response.Status.OK).build();
    }
}
