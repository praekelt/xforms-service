package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.core.JsonParser;
import io.dropwizard.setup.Environment;
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

    public FormsResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "getSingle()")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") int formId) {
        return Response.status(Response.Status.OK).build();
    }
    
    @Timed(name = "getAll()")
    @GET
    public Response getAll() {
        return Response.status(Response.Status.OK).build();
    }

}
