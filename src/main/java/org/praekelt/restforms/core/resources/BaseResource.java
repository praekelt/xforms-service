package org.praekelt.restforms.core.resources;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.praekelt.restforms.core.services.JedisClient;
import static org.praekelt.restforms.entry.RestformsService.jedisClient;

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
abstract class BaseResource {
    
    protected static Gson gson;
    protected static JedisClient jedis;
    
    protected BaseResource() {
        gson = (gson == null) ? new Gson() : gson;
        jedis = (jedis == null) ? jedisClient : jedis;
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
    
    protected String fetchResource(String key) {
        if (!key.isEmpty()) {
            return jedis.get(key);
        }
        return null;
    }
    
    protected String createResource(String json) {
        String id = this.generateUUID();
        
        if (!json.isEmpty()) {
            jedis.set(id, json);
            return id;
        }
        return null;
    }
}
