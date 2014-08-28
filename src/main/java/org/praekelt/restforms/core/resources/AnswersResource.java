package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import io.dropwizard.setup.Environment;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.praekelt.restforms.core.RestformsConfiguration;

/**
 *
 * @author ant cosentino
 */
public class AnswersResource extends BaseResource {
    
    public AnswersResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
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
