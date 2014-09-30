package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.validator.constraints.NotEmpty;
import org.praekelt.restforms.core.resources.AnswersResource.AnswersRepresentation.Answer;
import org.praekelt.restforms.core.services.jedis.JedisClient;

/**
 *
 * @author ant cosentino <ant@io.co.za>
 * @since 2014-09-20
 * @see org.praekelt.restforms.core.resources.BaseResource
 */
@Path("/answers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces({
    MediaType.APPLICATION_JSON,
    MediaType.APPLICATION_XML
})
public class AnswersResource extends BaseResource {
    
    /**
     * used to construct a json document representing
     * answers provided for xform documents.
     */
    public static class AnswersRepresentation {
        
        @NotEmpty
        private final String formUUID;
        
        @NotEmpty
        private final List<Answer> answers;
        
        @JsonCreator
        public AnswersRepresentation(
            @JsonProperty("formUUID") String formUUID,
            @JsonProperty("answers") List<Answer> answers
        ) {
            this.formUUID = formUUID;
            this.answers = answers;
        }
        
        @JsonProperty("formUUID")
        public String getFormUUID() {
            return formUUID;
        }
        
        @JsonProperty("answers")
        public List<Answer> getAnswers() {
            return answers;
        }
        
        /**
         * used to populate the list contained in
         * this class's parent class.
         */
        public static class Answer {
            
            @NotEmpty
            private final String ref;
            
            @NotEmpty
            private final String value;
            
            @JsonCreator
            public Answer(
                @JsonProperty("ref") String ref,
                @JsonProperty("value") String value
            ) {
                this.ref = ref;
                this.value = value;
            }
            
            @JsonProperty("ref")
            public String getRef() {
                return ref;
            }
            
            @JsonProperty("value")
            public String getValue() {
                return value;
            }
        }
    }
    
    public static class AnswersResponse extends BaseResponse {
        
        private String xForm;
        
        public AnswersResponse(int status, String message) {
            super(status, message);
        }
        
        public AnswersResponse(int status, String message, String xForm) {
            super(status, message);
            this.xForm = xForm;
        }

        public String getxForm() {
            return xForm;
        }

        public void setxForm(String xForm) {
            this.xForm = xForm;
        }
    }
    
    public AnswersResource(JedisClient jc) {
        super(jc);
//        this.hashPool = "answers-";
//        this.representation = AnswersRepresentation.class;
    }
    
    @Timed(name = "create")
    @POST
    public Response create(@Valid AnswersRepresentation ar) {
        String formId, xform, answersId;
        List<Answer> answers;
        
        formId = ar.getFormUUID();
        
        if (formId != null) {
            answers = ar.getAnswers();
            xform = "";//FormsResource.fetchFormValue(formId);
            
            if (xform != null) {
                
                // we'll just create a record in redis for now to
                // allow us to test this thing.
                
                answersId = "";//this.createResource(this.toJson(ar, representation));
                
                // this is where we'll invoke a method that processes
                // the answers provided for a xform and then add them
                // to our redis instance.
                
                if (answersId != null) {
                    return Response.status(Response.Status.CREATED).entity(
                        this.toJson(
                            new AnswersResponse(201, "Created completed xForm.", answersId),
                            AnswersResponse.class
                        )
                    ).build();
                }
                return Response.serverError().entity(
                    this.toJson(
                        new AnswersResponse(500, "A Redis error occurred while attempting to save the provided answers."),
                        AnswersResponse.class
                    )
                ).build();
            }
            return Response.status(Response.Status.NOT_FOUND).entity(
                this.toJson(
                    new AnswersResponse(404, "No xForm could be found with the provided UUID."),
                    AnswersResponse.class
                )
            ).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(
            this.toJson(
                new AnswersResponse(400, "No xForm UUID was provided."),
                AnswersResponse.class
            )
        ).build();
    }
    
    @Timed(name = "update")
    @PUT
    @Path("{answerId}")
    public Response update(
        @PathParam("answerId") String answersId,
        @Valid AnswersRepresentation ar
    ) {
        String existingAnswers, formId, xform;
        List<Answer> answers;
        boolean updated;
        
        // will eventually use this.verifyResource here
        existingAnswers = "";//this.fetchResource(answersId);
        
        if (existingAnswers != null) {
            formId = ar.getFormUUID();
            
            if (formId != null) {
                xform = "";//this.fetchResource(formId);

                if (xform != null) {

                    // we'll just update the record in redis for now to
                    // allow us to test this thing.
                    updated = false;//this.updateResource(answersId, this.toJson(ar, representation));
                    
                    // this is where we'll invoke a method that processes
                    // the answers provided for a xform and then add them
                    // to our redis instance.
                    answers = ar.getAnswers();
                    
                    if (updated) {
                        return Response.ok(
                            this.toJson(
                                new AnswersResponse(200, "Updated xForm.", answersId),
                                AnswersResponse.class
                            )
                        ).build();
                    }
                    return Response.serverError().entity(
                        this.toJson(
                            new AnswersResponse(500, "A Redis error occurred while attempting to update the specified record with the provided answers."),
                            AnswersResponse.class
                        )
                    ).build();
                }
                return Response.status(Response.Status.NOT_FOUND).entity(
                    this.toJson(
                        new AnswersResponse(500, "xForm not found."),
                        AnswersResponse.class
                    )
                ).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(
                this.toJson(
                    new AnswersResponse(400, "No xForm UUID was provided."),
                    AnswersResponse.class
                )
            ).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(
            this.toJson(
                new AnswersResponse(404, "xForm answers found."),
                AnswersResponse.class
            )
        ).build();
    }
    
    @Timed(name = "getSingle")
    @GET
    @Path("{answerId}")
    public Response getSingle(@PathParam("answerId") String answerId) {
        
        String xform = "";//this.fetchResource(answerId);
        
        if (xform != null) {
            return Response.ok(xform).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(
            this.toJson(
                new AnswersResponse(404, "No completed xForm record found."),
                AnswersResponse.class
            )
        ).type(MediaType.APPLICATION_JSON).build();
    }
}
