package org.praekelt.resources;

import com.google.common.base.Optional;
import com.codahale.metrics.annotation.Timed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.praekelt.models.Form;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class IndexResource {
    private final String template;
    private final String defaultName;

    public IndexResource(String template, String defaultName) {
        this.template = template;
        this.defaultName = defaultName;
    }

    @GET
    @Timed
    public Form sayHello(@QueryParam("name") Optional<String> name) {
        final String value = String.format(template, name.or(defaultName));
        return new Form(value);
    }
}