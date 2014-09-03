package org.praekelt.restforms.core.resources;

import com.codahale.metrics.annotation.Timed;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Iterator;
import java.util.Set;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.hibernate.validator.constraints.NotEmpty;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
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
public class FormsResource extends BaseResource {
    
    /**
     * used to construct a json document containing
     * all xforms stored within our redis instance.
     */
    public static class FormsRepresentation {
        
        @NotEmpty
        private String uuid;
        
        @NotEmpty
        private String xml;
        
        @JsonCreator
        public FormsRepresentation() {}
        
        @JsonCreator
        public FormsRepresentation(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("xml") String xml
        ) {
            this.uuid = uuid;
            this.xml = xml;
        }
        
        @JsonProperty("uuid")
        public String getUuid() {
            return uuid;
        }
        
        @JsonProperty("uuid")
        public void setUuid(String uuid) {
            this.uuid = uuid;
        }
        
        @JsonProperty("xml")
        public String getXml() {
            return xml;
        }
        
        @JsonProperty("xml")
        public void setXml(String xml) {
            this.xml = xml;
        }
    }
    
    public static class FormsResponse extends BaseResponse {
        
        private FormsRepresentation[] xforms;
        private String xform;
        private Integer count;
        
        public FormsResponse(int status, String message) {
            super(status, message);
            this.count = null;
        }
        
        public FormsResponse(int status, String message, String xform) {
            super(status, message);
            this.xform = xform;
            this.count = null;
        }
        
        public FormsResponse(int status, String message, int count, FormsRepresentation[] xforms) {
            super(status, message);
            this.count = count;
            this.xforms = xforms;
        }
        
        public FormsRepresentation[] getXforms() {
            return xforms;
        }

        public void setXforms(FormsRepresentation[] xforms) {
            this.setXforms(xforms);
        }

        public String getXform() {
            return xform;
        }

        public void setXform(String xform) {
            this.xform = xform;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
    
    public FormsResource(JedisClient jc) {
        super(jc);
        this.representation = FormsRepresentation.class;
    }
    
    @Timed(name = "create")
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    public Response create(String payload) {
        
        final String id;
        
        if (!payload.isEmpty()) {
            id = this.createResource(payload);
        
            if (id != null) {
                return Response.status(Response.Status.CREATED).entity(
                    this.toJson(
                        new FormsResponse(201, "Created xForm.", this.fetchResource(id)),
                        FormsResponse.class
                    )
                ).build();
            }
            return Response.serverError().entity(
                this.toJson(
                    new FormsResponse(500, "A Redis error occurred while attempting to save the provided xForm."),
                    FormsResponse.class
                )
            ).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(
            this.toJson(
                new FormsResponse(400, "No request payload was provided."),
                FormsResponse.class
            )
        ).build();
    }
    
    @Timed(name = "getSingle")
    @GET
    @Path("{formId}")
    public Response getSingle(@PathParam("formId") String formId) {
        String xform = this.fetchResource(formId);
        
        if (xform != null) {
            return Response.ok(xform).type(MediaType.APPLICATION_XML).build();
        }
        return Response.status(Response.Status.NOT_FOUND).entity(
            this.toJson(new FormsResponse(404, "xForm not found.", null), FormsResponse.class)
        ).type(MediaType.APPLICATION_JSON).build();
    }
    
    @Timed(name = "getAll")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll() {
        int key;
        Iterator i;
        String current, form;
        FormsRepresentation fr;
        
        Set<String> keys = jedis.getKeys();
        int keyCount = keys.size();
        FormsRepresentation[] forms = new FormsRepresentation[keyCount];
        
        if (keyCount > 0) {
            i = jedis.getKeys().iterator();
            key = 0;
            
            while (i.hasNext()) {
                current = i.next().toString();
                form = this.fetchResource(current);
                fr = new FormsRepresentation();
                fr.setUuid(current);
                fr.setXml(form);
                forms[key++] = fr;
            }
        }
        return Response.ok().entity(
            this.toJson(new FormsResponse(200, "Success.", keyCount, forms), FormsResponse.class)
        ).build();
    }
}
