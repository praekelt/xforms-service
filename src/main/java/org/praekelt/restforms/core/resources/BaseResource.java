package org.praekelt.restforms.core.resources;

import com.google.gson.Gson;
import io.dropwizard.setup.Environment;
import java.lang.reflect.Type;
import java.util.UUID;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
 */
public abstract class BaseResource {
    
    protected static final Gson gson = new Gson();
    protected static JedisClient jedisClient;
    
    protected BaseResource(RestformsConfiguration cfg, Environment env) {
        
        if (jedisClient == null) {
            jedisClient = cfg.getJedisFactory().build(env);
        }
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
}
