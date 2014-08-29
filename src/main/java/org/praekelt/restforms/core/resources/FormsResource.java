package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
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
public class FormsResource extends BaseResource implements BaseResource.Representable {
    
    private static class FormsRepresentation {
        public FormsRepresentation() {}
        
        //an important note:
        //
        //any properties of this class should have
        //a SerializedName annotation, even if it is
        //identical to the property name. this will
        //allow us to rename our properties even once
        //we have decided upon a strict document format.
    }
    
    public FormsResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Override
    public String to(Object base) {
        return gson.toJson(base, FormsRepresentation.class);
    }

    @Override
    public Object from(String json) {
        return gson.fromJson(json, FormsRepresentation.class);
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
