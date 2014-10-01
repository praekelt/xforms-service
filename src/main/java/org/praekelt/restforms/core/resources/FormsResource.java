package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.praekelt.restforms.core.exceptions.RosaException;
import org.praekelt.restforms.core.services.jedis.JedisClient;
import org.praekelt.restforms.core.services.rosa.RosaFactory;

/**
 * @author ant cosentino
 */
@Path("/forms")
@Consumes({
    MediaType.APPLICATION_JSON,
    MediaType.APPLICATION_XML
})
@Produces({
    MediaType.APPLICATION_JSON,
    MediaType.APPLICATION_XML
})
public final class FormsResource extends BaseResource {
    
    public FormsResource(JedisClient jc) {
        super(jc);
        responseEntity = FormsResponse.class;
    }
    
    public static class FormsResponse extends BaseResponse {

        private String id;

        public FormsResponse(int status, String message) {
            super(status, message);
        }
        
        public FormsResponse(int status, String message, String id) {
            this(status, message);
            this.id = id;
        }
        
        public String getId() { return id; }
        
        public void setId(String id) {
            this.id = id;
        }
    }

    @Timed(name = "create")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response create(String payload) {
        String id;
        
        if ("".equals(payload)) {
            // we're outta here
            return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                new FormsResponse(400, "No XML payload was provided in the request."),
                responseEntity
            )).build();
        }
        
        try {
            if ((id = createResource("form", payload)) != null) {

                RosaFactory r = new RosaFactory();

                if (r.setUp(payload)) {

                    if (updateResource(id, RosaFactory.persist(r))) {
                        return Response.status(Response.Status.CREATED).entity(toJson(
                            new FormsResponse(201, "Created XForm.", id),
                            responseEntity
                        )).build();
                    }
                }
            }
            return Response.serverError().entity(toJson(
                new FormsResponse(500, "An error occurred while attempting to save the provided XForm. Please ensure the XML you provided is well-formed and valid."),
                responseEntity
            )).build();
        } catch (InternalServerErrorException e) {
            return Response.serverError().entity(toJson(
                new FormsResponse(500, "An error occurred while attempting to save the provided XForm: " + e.getMessage() + "."),
                responseEntity
            )).build();
        } catch (RosaException e) {
            return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                new FormsResponse(400, "An error occurred while attempting to save the provided XForm. Please ensure the XML you provided is well-formed and valid."),
                responseEntity
            )).build();
        }
    }
    
    @Timed(name = "getSingle")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") String formId) {
        
        try {
            String xform = fetchField(formId, "form");
        
            if (xform != null) {
                return Response.ok(xform).type(MediaType.APPLICATION_XML).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(toJson(
                new FormsResponse(404, "No XForm was found associated with the given ID."),
                responseEntity
            )).type(MediaType.APPLICATION_JSON).build();
        } catch (InternalServerErrorException e) {
            return Response.serverError().entity(toJson(
                new FormsResponse(500, "A XForm processing error occurred: " + e.getMessage() + "."),
                responseEntity
            )).build();
        }
    }
}