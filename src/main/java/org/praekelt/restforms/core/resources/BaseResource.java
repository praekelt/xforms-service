package org.praekelt.restforms.core.resources;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;
import javax.ws.rs.InternalServerErrorException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.praekelt.restforms.core.exceptions.JedisException;
import org.praekelt.restforms.core.services.jedis.JedisClient;

/**
 * @author ant cosentino <ant@io.co.za>
 * @since 2014-09-20
 */
abstract class BaseResource {
    
    private static final Logger logger = Logger.getLogger("BaseResource");
    protected static JedisClient jedis;
    protected static Gson gson;
    protected Type requestEntity;
    protected Type responseEntity;

    protected BaseResource(JedisClient jc) {
        gson = (gson == null) ? new GsonBuilder().disableHtmlEscaping().create() : gson;
        jedis = (jedis == null) ? jc : jedis;
    }

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

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    /**
     * serialises the object as json
     * 
     * @param base an instance of the static inner class for the resource
     * @param type class literal of static inner class for the resource
     * @return string json representation of the object
     */
    protected static String toJson(Object base, Type type) {
        return (base != null && type != null) ? gson.toJson(base, type) : null;
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
    protected static Object fromJson(String json, Type type) {
        return (!"".equals(json) && json != null && type != null) ? gson.fromJson(json, type) : null;
    }

    /**
     * determine the existence of a value (of any type)
     * stored at `key`.
     *
     * @param key
     * @return boolean
     */
    protected boolean verifyResource(String key) throws InternalServerErrorException {
        
        try {
            return jedis.keyExists(key);
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#verifyResource(): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }

    /**
     * determine the existence of a value at `field`
     * stored in a hash at key.
     *
     * @param key
     * @param field
     * @return boolean
     */
    protected boolean verifyField(String key, String field) throws InternalServerErrorException {

        try {
            return this.verifyResource(key) ? jedis.hashFieldExists(key, field) : false;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#verifyField(): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }
    
    /**
     * 
     * 
     * @param key
     * @param field
     * @return string
     */
    protected String fetchField(String key, String field) throws InternalServerErrorException {

        try {
            return this.verifyResource(key) ? jedis.hashGetFieldValue(key, field) : null;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#fetchField(): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }

    /**
     * gets a key/value mapping of fields to values from a hash at the given key.
     * returns a string/string map if exists.
     *
     * @param key
     * @return string/string map
     */
    protected Map<String, String> fetchResource(String key) throws InternalServerErrorException {
        
        try {
            return this.verifyResource(key) ? jedis.hashGetFieldsAndValues(key) : null;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#fetchResource(): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }

    /**
     * gets the value stored at field "object" of a hash at the given id.
     * returns a byte[] if exists.
     *
     * @param id
     * @return byte[]
     */
    protected byte[] fetchPOJO(String key) throws InternalServerErrorException {

        try {
            return this.verifyResource(key) ? jedis.hashGetPOJO(key) : null;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#fetchPOJO(): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }

    /**
     * stores the given value at the given field of a hash.
     * generates an id (prepended with the given hash pool) to key the hash.
     * returns the id if created.
     *
     * @param hashPool
     * @param type
     * @param value
     * @return string
     */
    protected String createResource(String field, String value) throws InternalServerErrorException {
        
        try {
            String key = this.generateUUID();
            return jedis.hashSetFieldValue(key, field, value) ? key : null;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#createResource(): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }
    
    /**
     * updates the value at the given field of a hash stored at the given key.
     * returns true if updated.
     *
     * @param id
     * @param type
     * @param value
     * @return boolean
     */
    protected boolean updateResource(String key, String type, String value) throws InternalServerErrorException {
        
        try {
            return this.verifyResource(key) ? jedis.hashSetFieldValue(key, type, value) : false;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#updateResource(key, type, value): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }

    /**
     * updates the given byte[] value at field "object" of the hash at the given key.
     * returns true if updated.
     *
     * @param id
     * @param pojo
     * @return boolean
     */
    protected boolean updateResource(String key, byte[] pojo) throws InternalServerErrorException {

        try {
            return this.verifyResource(key) ? jedis.hashSetPOJO(key, pojo) : false;
        } catch (JedisException e) {
            logger.log(
                Level.ERROR,
                "BaseResource#updateResource(key, pojo): a JedisException occurred. Trace: " + e.getMessage()
            );
            throw new InternalServerErrorException("Data-store is unreachable.");
        }
    }
}
