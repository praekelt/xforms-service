package org.praekelt.restforms.core.resources;

import com.google.gson.Gson;
import io.dropwizard.setup.Environment;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
 */
public abstract class BaseResource {
    
    protected static final Gson gson = new Gson();
    protected static JedisClient jedisClient;
    
    protected static interface Representable {
        String to(Object base);
        Object from(String json);
    }
        
    protected BaseResource(RestformsConfiguration cfg, Environment env) {
        
        if (jedisClient == null) {
            jedisClient = cfg.getJedisFactory().build(env);
        }
    }
}
