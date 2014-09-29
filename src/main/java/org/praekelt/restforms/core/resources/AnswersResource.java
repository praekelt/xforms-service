package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.praekelt.restforms.core.exceptions.JedisException;
import org.praekelt.restforms.core.services.jedis.JedisClient;

/**
 * @author ant cosentino
 */
@Path("/answers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_XML)
public final class AnswersResource extends BaseResource {
    
    private static final Logger logger = Logger.getLogger("AnswersResource");
    
    public AnswersResource(JedisClient jc) {
        super(jc);
        responseEntity = AnswersResponse.class;
    }

    public static class AnswersResponse extends BaseResponse {

        public AnswersResponse(int status, String message) {
            super(status, message);
        }
    }

    @Timed(name = "getSingle")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") String formId) {
        
        try {
            String modelInstance = fetchModelInstance(formId);
            
            if (modelInstance != null && !"".equals(modelInstance)) {
                return Response.ok(modelInstance).type(MediaType.APPLICATION_XML).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(toJson(
                new AnswersResponse(404, "No XForm was found to be associated with the given ID. This could also indicate server error."),
                responseEntity
            )).type(MediaType.APPLICATION_JSON).build();
        } catch (InternalServerErrorException e) {
            return Response.serverError().entity(toJson(
                new AnswersResponse(500, "A server error occurred: " + e.getMessage() + "."),
                responseEntity
            )).type(MediaType.APPLICATION_JSON).build();
        }
    }

    /**
     * returns the model/instance data that is associated with the given key.
     *
     * @param key
     * @return string
     */
    private String fetchModelInstance(String key) {
        
        try {
            return (verifyResource(key)) ? jedis.hashGetFieldValue(key, "completed") : null;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "AnswersResource#fetchModelInstance(key): a JedisException occurred. Exception message: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }
}
