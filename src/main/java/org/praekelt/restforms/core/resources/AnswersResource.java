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
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
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
    public class AnswersRepresentation {
        
        @NotEmpty
        private String formUUID;
        
        @NotEmpty
        private List<Answer> answers;
        
        public AnswersRepresentation() {}
        
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
        
        @JsonProperty("formUUID")
        public void setFormUUID(String formUUID) {
            this.formUUID = formUUID;
        }
        
        @JsonProperty("answers")
        public List<Answer> getAnswers() {
            return answers;
        }
        
        @JsonProperty("answers")
        public void setAnswers(List<Answer> answers) {
            this.answers = answers;
        }
        
        /**
         * used to populate the list contained in
         * this class's parent class.
         */
        public class Answer {
            
            @NotEmpty
            private String ref;
            
            @NotEmpty
            private String value;
            
            @JsonProperty("ref")
            public String getRef() {
                return ref;
            }
            
            @JsonProperty("ref")
            public void setRef(String ref) {
                this.ref = ref;
            }
            
            @JsonProperty("value")
            public String getValue() {
                return value;
            }
            
            @JsonProperty("value")
            public void setValue(String value) {
                this.value = value;
            }
        }
    }
    
    public AnswersResource(JedisClient jc) {
        super(jc);
        this.representationType = AnswersRepresentation.class;
    }
    
    @Timed(name = "create")
    @POST
    public Response create(@Valid AnswersRepresentation ar) {
        String formId, xform;
        List<Answer> answers;
        
        formId = ar.getFormUUID();

        if (formId != null) {
            answers = ar.getAnswers();
            xform = this.fetchResource(formId);

            if (xform != null) {

                // this is where we'll invoke a method that processes
                // the answers provided for a xform and then add them
                // to our redis instance.

                return Response.status(Response.Status.CREATED).entity(
                    String.format(
                        "{\"%s\": %d, \"%s\": \"%s\", \"%s\": %s}",
                        "status", 201, "message", "Created completed xForm", "answer", "{}"
                    )
                ).build();
            }
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                String.format(
                    "{\"%s\": %d, \"%s\": \"%s\"}",
                    "status", 500, "message", "A Redis error occurred while attempting to retrieve the xForm template."
                )
            ).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(
            String.format(
                "{\"%s\": %d, \"%s\": \"%s\"}",
                "status", 400, "message", "No xForm UUID was provided."
            )
        ).build();
    }
    
    @Timed(name = "update")
    @PUT
    @Path("{answerId}")
    public Response update(@PathParam("answerId") String answerId, @Valid AnswersRepresentation ar) {
        
        String existing = this.fetchResource(answerId);
        String formId, xform;
        List<Answer> answers;

        formId = ar.getFormUUID();

        if (existing != null) {

            if (formId != null) {
                answers = ar.getAnswers();
                xform = this.fetchResource(formId);

                if (xform != null) {

                    // this is where we'll invoke a method that processes
                    // the answers provided for a xform and then add them
                    // to our redis instance.

                    return Response.ok(
                        String.format(
                            "{\"%s\": %d, \"%s\": \"%s\", \"%s\": %s}",
                            "status", 200, "message", "Updated answer", "answer", "{}"
                        )
                    ).build();
                }
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(
                    String.format(
                        "{\"%s\": %d, \"%s\": \"%s\"}",
                        "status", 500, "message", "A Redis error occurred while attempting to retrieve the completed xForm."
                    )
                ).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(
                String.format(
                    "{\"%s\": %d, \"%s\": \"%s\"}",
                    "status", 400, "message", "No xForm UUID was provided."
                )
            ).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(
            String.format(
                "{\"%s\": %d, \"%s\": \"%s\"}",
                "status", 400, "message", "No completed xForm record found."
            )
        ).build();
    }
    
    @Timed(name = "getSingle")
    @GET
    @Path("{answerId}")
    @Produces(MediaType.APPLICATION_XML)
    public Response getSingle(@PathParam("answerId") String answerId) {
        
        String xform = this.fetchResource(answerId);
        
        if (xform != null) {
            return Response.ok(xform).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(
            String.format(
                "{\"%s\": %d, \"%s\": \"%s\"}",
                "status", 404, "message", "Completed xForm not found."
            )
        ).type(MediaType.APPLICATION_JSON).build();
    }
}
