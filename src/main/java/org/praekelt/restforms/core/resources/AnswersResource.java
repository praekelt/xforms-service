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
public class AnswersResource extends BaseResource {
    
    private static class AnswersRepresentation {
        public AnswersRepresentation() {}
        
        //an important note:
        //
        //any properties of this class should have
        //a SerializedName annotation, even if it is
        //identical to the property name. this will
        //allow us to safely rename our properties
        //once we have decided upon a strict document format.
    }
    
    public AnswersResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        //example:
        
        //use the payload to construct a pojo representing the json document provided in the http request
        AnswersRepresentation ar = (AnswersRepresentation) this.fromJson(payload, AnswersRepresentation.class);
        
        //clean up/validate any data contained in the pojo
        // if (dataIsDirty(ar)) {
        //  reject
        // } else {
        //  store in db
        // }
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "update(id)")
    @PUT
    @Path("{formId}")
    public Response update(@PathParam("formId") String formId, String payload) {
        //example:
        
        //same as above...
        //we'd create a new answersrepresentation and append any data provided in the payload to it
        //then validate and store in the db if successful
        
        return Response.status(Response.Status.OK).build();
    }
}
