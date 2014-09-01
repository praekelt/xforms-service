package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
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
@Path("/answers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnswersResource extends BaseResource {
    
    private static class AnswersRepresentation {
        public AnswersRepresentation() {}
    }
    
    public AnswersResource(JedisClient jc) {
        super(jc);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "update(id)")
    @PUT
    @Path("{formId}")
    public Response update(@PathParam("formId") String formId, String payload) {
        return Response.ok().build();
    }
}
