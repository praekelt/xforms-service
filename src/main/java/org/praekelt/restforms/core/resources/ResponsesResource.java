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
        
        private String response;
        private int question;
        
        @JsonCreator
        public ResponsesRepresentation(
            @JsonProperty("response") String response,
            @JsonProperty("question") int question
        ) {
            this.response = response;
            this.question = question;
        }
        
        @JsonCreator
        public ResponsesRepresentation(@JsonProperty("question") int question) {
            this.response = null;
            this.question = question;
        }
        
        @JsonProperty("response")
        public String getResponse() { return response; }
        
        @JsonProperty("question")
        public int getQuestion() { return question; }
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
        String nextQuestion, answer = r.getResponse();
        byte[] fromPersist, toPersist;
        RosaFactory rf;
        
        try {
            
            if (answer != null) {
                
                if (verifyResource(formId)) {
                    fromPersist = fetchPOJO(formId);
                    rf = (fromPersist != null) ? RosaFactory.rebuild(fromPersist) : null;
                    
                    if (rf != null && rf.setUp()) {
                        
                        if (rf.getCompleted() < rf.getTotal()) {
                            nextQuestion = rf.getQuestion(rf.answerQuestion(answer));
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
                        return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                            new ResponsesResponse(400, "The XForm you identified has already been completed."),
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
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                new ResponsesResponse(400, "No `response` field was provided in the request payload."),
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
    @Path("{formId}")
    public Response getQuestion(
        @PathParam("formId") String formId,
        ResponsesRepresentation r
    ) {
        int questionIndex = r.getQuestion();
        String question;
        RosaFactory rf;
        byte[] serialised;

        if (verifyResource(formId)) {

            try {
                serialised = fetchPOJO(formId);
                rf = serialised != null ? RosaFactory.rebuild(serialised) : null;

                if (rf != null && rf.setUp()) {

                    if (rf.getCompleted() < rf.getTotal()) {
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
                    return Response.status(Response.Status.BAD_REQUEST).entity(toJson(
                        new ResponsesResponse(400, "The XForm you identified has already been completed."),
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
        return Response.status(Response.Status.NOT_FOUND).entity(toJson(
            new ResponsesResponse(404, "No XForm was found associated with the given ID."),
            responseEntity
        )).build();
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