package org.praekelt.restforms.core.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import io.dropwizard.setup.Environment;
import java.lang.reflect.Type;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * this class serves as a base for
 * all of our server's endpoints.
 * it bakes in common utilities needed
 * by all of our endpoints for tasks
 * like json serialisation or id generation.
 * 
 * 
 * @author ant cosentino
 */
public abstract class BaseResource {
    
    protected static Gson gson;
    protected static ObjectMapper om;
    protected static JedisClient jedisClient;
    
    protected BaseResource(RestformsConfiguration cfg, Environment env) {
        om = (om == null) ? env.getObjectMapper() : om;
        gson = (gson == null) ? new Gson() : gson;
        jedisClient = (jedisClient == null) ? cfg.getJedisFactory().build(env) : jedisClient;
    }
    
    protected String toJson(Object base, Type type) {
        return gson.toJson(base, type);
    }
    
    protected Object fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
    
    protected String generateUUID() {
        return UUID.randomUUID().toString();
    }
    
    protected String implode(String[] array, char separator) {
        return StringUtils.join(array, separator);
    }
}
