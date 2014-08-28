package org.praekelt.restforms.core.resources;

import io.dropwizard.setup.Environment;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.services.JedisClient;

/**
 *
 * @author ant cosentino
 */
public abstract class BaseResource {
    
    private static JedisClient jedisClient;
    
    protected BaseResource(RestformsConfiguration cfg, Environment env) {
        
        if (jedisClient == null) {
            BaseResource.jedisClient = cfg.getJedisFactory().build(env);
        }
    }
}
