package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Path("/responses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public final class ResponsesResource extends BaseResource {
    
    public ResponsesResource(JedisClient jc) {
        super(jc);
        requestEntity = ResponsesRepresentation.class;
        responseEntity = ResponsesResponse.class;
    }

    public static class ResponsesRepresentation {
        
        private String answer;
        
        @JsonCreator
        public ResponsesRepresentation(@JsonProperty("answer") String answer) {
            this.answer = answer;
        }
        
        @JsonProperty("answer")
        public String getAnswer() { return answer; }
    }
    
    public static class ResponsesResponse extends BaseResponse {
        
        private String id;
        private String question;
        
        public ResponsesResponse(int status, String message) {
            super(status, message);
        }
        
        public ResponsesResponse(int status, String message, String id, String question) {
            this(status, message);
            this.id = id;
            this.question = question;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getQuestion() {
            return question;
        }

        public void setQuestion(String question) {
            this.question = question;
        }
    }

    @Timed(name = "answerQuestion")
    @POST
    @Path("{formId}")
    public Response answerQuestion(
        @PathParam("formId") String formId,
        ResponsesRepresentation r
    ) {
        String nextQuestion, answer = r.getAnswer();
        byte[] fromPersist, toPersist;
        RosaFactory rf;
        
        if (answer == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                new ResponsesResponse(400, "No `answer` field was provided in the request payload."),
                responseEntity
            )).build();
        }
        
        try {
            fromPersist = fetchPOJO(formId);
            
            if (fromPersist == null) {
                return Response.status(Response.Status.NOT_FOUND).entity(toJson(
                    new ResponsesResponse(404, "No XForm was found associated with the given ID."),
                    responseEntity
                )).build();
            }
            
            rf = RosaFactory.rebuild(fromPersist);

            if (rf != null && rf.setUp()) {
                
                if (rf.getCompleted() >= rf.getTotal()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                        new ResponsesResponse(400, "The XForm you identified has already been completed."),
                        responseEntity
                    )).build();
                }
                
                nextQuestion = rf.getQuestion(rf.processAnswer(answer));
                toPersist = RosaFactory.persist(rf);

                if (updateResource(formId, toPersist)) {

                    if (nextQuestion != null) {
                        return Response.ok(toJson(
                            new ResponsesResponse(200, "Question completed.", formId, nextQuestion),
                            responseEntity
                        )).build();
                    }
                    
                    if (createModelInstance(formId, rf.getCompletedXForm())) {
                        return Response.ok(toJson(
                            new ResponsesResponse(200, "XForm completed.", formId, null),
                            responseEntity
                        )).build();
                    }
                }
                return Response.serverError().entity(toJson(
                    new ResponsesResponse(500, "A XForm processing error occurred."),
                    responseEntity
                )).build();
            }
            return Response.serverError().entity(toJson(
                new ResponsesResponse(500, "A XForm processing error occurred."),
                responseEntity
            )).build();
        } catch (RosaException e) {
            return Response.serverError().entity(toJson(
                new ResponsesResponse(500, "A XForm processing error occurred: " + e.getMessage()),
                responseEntity
            )).build();
        } catch (InternalServerErrorException e) {
            return Response.serverError().entity(toJson(
                new ResponsesResponse(500, "A XForm processing error occurred: " + e.getMessage()),
                responseEntity
            )).build();
        }
    }

    @Timed(name = "getQuestion")
    @GET
    @Path("{formId}/{questionIndex}")
    public Response getQuestion(
        @PathParam("formId") String formId,
        @PathParam("questionIndex") int questionIndex
    ) {
        String question;
        RosaFactory rf;
        byte[] serialised;
        
        try {
            
            if ((serialised = fetchPOJO(formId)) != null) {
                rf = RosaFactory.rebuild(serialised);

                if (rf.getCompleted() >= rf.getTotal()) {
                    return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                        new ResponsesResponse(400, "The XForm you identified has already been completed."),
                        responseEntity
                    )).build();
                }
                
                if (rf.setUp()) {
                    question = rf.getQuestion(questionIndex);

                    if (question != null) {
                        return Response.ok(toJson(
                            new ResponsesResponse(200, "Question retrieved successfully.", formId, question),
                            responseEntity
                        )).build();
                    }
                    return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                        new ResponsesResponse(400, "The question you requested was out of bounds. Please try again."),
                        responseEntity
                    )).build();
                }
                return Response.serverError().entity(toJson(
                    new ResponsesResponse(500, "A XForm processing error occurred."),
                    responseEntity
                )).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(toJson(
                new ResponsesResponse(404, "No XForm was found associated with the given ID."),
                responseEntity
            )).build();
        } catch (RosaException e) {
            return Response.serverError().entity(toJson(
                new ResponsesResponse(500, "A XForm processing error occurred: " + e.getMessage()),
                responseEntity
            )).build();
        } catch (InternalServerErrorException e) {
            return Response.serverError().entity(toJson(
                new ResponsesResponse(500, "A XForm processing error occurred: " + e.getMessage()),
                responseEntity
            )).build();
        }
    }

    /**
     * creates a new field in the hash associated
     * with the given id containing the model/
     * instance data of the completed xform
     *
     * @param string
     * @param string
     * @return boolean
     */
    private boolean createModelInstance(String key, String xml) {
        return verifyResource(key) ? updateResource(key, "completed", xml) : false;
    }
}