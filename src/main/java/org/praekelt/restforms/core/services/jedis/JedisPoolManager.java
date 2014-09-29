package org.praekelt.restforms.core.services.jedis;

import io.dropwizard.lifecycle.Managed;
import redis.clients.jedis.JedisPool;

/**
 * a manager to control the lifecycle of
 * the redis connection pool
 * 
 * @author ant cosentino
 */
public class JedisPoolManager implements Managed {

    private final JedisPool pool;

    public JedisPoolManager(JedisPool pool) {
        this.pool = pool;
    }
    
    @Override
    public void start() throws Exception {}

    @Override
    public void stop() throws Exception {
        pool.destroy();
    }
}
