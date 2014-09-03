package org.praekelt.restforms.core.resources;

import com.google.gson.Gson;
import java.lang.reflect.Type;
import java.util.UUID;
import org.apache.commons.lang.StringUtils;
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
        // this will be properly implemented in the
        // jedis development branch. until then, this
        // stub will always return false.
        
        //return jedis.exists(key);
        return false;
    }
    
    protected String fetchResource(String key) {
        
        if (!key.isEmpty()) {
            return jedis.get(key);
        }
        return null;
    }
    
    protected String createResource(String json) {
        
        String id;
        
        if (!json.isEmpty()) {
            id = this.generateUUID();
            jedis.set(id, json);
            return id;
        }
        return null;
    }
    
    protected boolean updateResource(String id, String json) {
        // a good place to use this.verifyResource()
        
        if (!json.isEmpty() && !id.isEmpty()) {
            jedis.set(id, json);
            return true;
        }
        return false;
    }
    
    private String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
