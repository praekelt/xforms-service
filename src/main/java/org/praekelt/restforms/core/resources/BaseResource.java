package org.praekelt.restforms.core.resources;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.Set;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
import org.praekelt.restforms.core.exceptions.JedisException;
import org.praekelt.restforms.core.services.jedis.JedisClient;

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
    protected Type representation;
    
    protected static class BaseResponse {
        
        private int status;
        private String message;
        
        public BaseResponse(int status, String message) {
            this.status = status;
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
    
    protected BaseResource(JedisClient jc) {
        gson = (gson == null) ? new Gson() : gson;
        jedis = (jedis == null) ? jc : jedis;
    }
    
    /**
     * serialises the object as json
     * 
     * @param base an instance of the static inner class for the resource
     * @param type class literal of static inner class for the resource
     * @return string json representation of the object
     */
    protected String toJson(Object base, Type type) {
        return gson.toJson(base, type);
    }
    
    /**
     * deserialises the object from a json string.
     * it will be necessary to manually cast the
     * returned object to the correct type.
     * 
     * @param json string representation of the object
     * @param type class literal of static inner class for the resource
     * @return object deserialised representation of the given json argument
     */
    protected Object fromJson(String json, Type type) {
        return gson.fromJson(json, type);
    }
    
    protected String implode(String[] array, char separator) {
        
        if (array.length > 0) {
            return StringUtils.join(array, separator);
        }
        return null;
    }
    
    protected boolean verifyResource(String key) {
        try {
            boolean exists;
            
            if (!key.isEmpty()) {
                exists = jedis.exists(key);
                return exists;
            }
        } catch (JedisException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
    
    protected String fetchResource(String key) {
        
        try {
            String resource;
            
            if (!key.isEmpty()) {
                resource = jedis.get(key);
                return resource;
            }
        } catch (JedisException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    
    protected Set<String> fetchResources() {
        
        try {
            Set<String> keys;
            
            keys = jedis.getKeys("*");
            return keys;
        } catch (JedisException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    
    protected String createResource(String json) {
        
        try {
            String id;

            if (!json.isEmpty()) {
                id = this.generateUUID();
                jedis.set(id, json);
                return id;
            }
        } catch (JedisException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
    
    protected boolean updateResource(String id, String json) {
        // a good place to use this.verifyResource()
        
        try {
            
            if (!json.isEmpty() && !id.isEmpty()) {
                jedis.set(id, json);
                return true;
            }
        } catch (JedisException e) {
            System.err.println(e.getMessage());
        }
        return false;
    }
    
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
