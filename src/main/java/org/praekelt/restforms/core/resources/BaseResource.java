package org.praekelt.restforms.core.resources;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.setup.Environment;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
 */
public abstract class BaseResource {
    
    protected static final JsonFactory jsonFactory = new JsonFactory();
    protected static final ObjectMapper objectMapper = new ObjectMapper(jsonFactory);
    private static JedisClient jedisClient;
    
    protected BaseResource(RestformsConfiguration cfg, Environment env) {
        
        if (jedisClient == null) {
            BaseResource.jedisClient = cfg.getJedisFactory().build(env);
        }
    }
}
