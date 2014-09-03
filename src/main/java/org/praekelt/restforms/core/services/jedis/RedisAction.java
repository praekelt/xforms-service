/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praekelt.restforms.core.services.jedis;

import redis.clients.jedis.Jedis;

/**
 *
 * @author simon kelly
 * @param <T>
 */
public abstract class RedisAction<T> {
    
    public abstract T execute(Jedis jedis) throws Exception;

    public T handleException(Exception e) throws Exception {
        throw e;
    }
}
