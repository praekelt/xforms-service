package org.praekelt.restforms.core.services.jedis;

import redis.clients.jedis.Jedis;

/**
 * a set of behaviours to reduce code repetition 
 * and help standardise the exceptions thrown 
 * by the jedisclient class 
 * 
 * @author simon kelly
 * @param <T>
 */
public abstract class JedisAction<T> {
    
    public abstract T execute(Jedis jedis) throws Exception;

    public T handleException(Exception e) throws Exception {
        throw e;
    }
}
