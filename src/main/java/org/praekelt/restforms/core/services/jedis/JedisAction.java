package org.praekelt.restforms.core.services.jedis;

import redis.clients.jedis.Jedis;

/**
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
