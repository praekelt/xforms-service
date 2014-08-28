package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.setup.Environment;
import java.io.IOException;
import java.util.Iterator;
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
    
    public AnswersResource(RestformsConfiguration cfg, Environment env) {
        super(cfg, env);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        
        try {
            //jsonnode can represent an entire json document
            //we retrieve an iterator and work our way across
            //the parsed object, performing our work as we need to.
            //we'll factor this logic out into a private method
            //once we're sure of it's behaviour.
            JsonNode parsedValue = objectMapper.readTree(payload);
            
            Iterator i = parsedValue.elements();
            while (i.hasNext()) {
                System.out.println(i.next().toString());
            }
            
        } catch (IOException ioe) {
            System.err.println("caught a json parsing exception");
        }
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "update(id)")
    @PUT
    @Path("{formId}")
    public Response update(@PathParam("formId") int formId, String payload) {
        return Response.status(Response.Status.OK).build();
    }
}
