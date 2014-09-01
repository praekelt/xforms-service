package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import java.util.List;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.praekelt.restforms.core.resources.AnswersResource.AnswersRepresentation.Answer;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
 */
@Path("/answers")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AnswersResource extends BaseResource {
    
    static class AnswersRepresentation {
        private String formUUID;
        private List<Answer> answers;
        
        public String getFormUUID() {
            return formUUID;
        }
        
        public void setFormUUID(String formUUID) {
            this.formUUID = formUUID;
        }
        
        public List<Answer> getAnswers() {
            return answers;
        }
        
        public void setAnswers(List<Answer> answers) {
            this.answers = answers;
        }
        
        public static class Answer {
            private String ref;
            private String value;

            public String getRef() {
                return ref;
            }
            
            public void setRef(String ref) {
                this.ref = ref;
            }
            
            public String getValue() {
                return value;
            }
            
            public void setValue(String value) {
                this.value = value;
            }
        }
    }
    
    public AnswersResource(JedisClient jc) {
        super(jc);
    }
    
    @Timed(name = "create()")
    @POST
    public Response create(String payload) {
        
        AnswersRepresentation ar = (AnswersRepresentation) this.fromJson(payload, AnswersRepresentation.class);
        
        for (Answer a : ar.answers) {
            System.out.println("the ref: " + a.getRef());
            System.out.println("the value: " + a.getValue());
        }
        
        return Response.status(Response.Status.CREATED).build();
    }
    
    @Timed(name = "update(id)")
    @PUT
    @Path("{formId}")
    public Response update(@PathParam("formId") String formId, String payload) {
        return Response.ok().build();
    }
}
