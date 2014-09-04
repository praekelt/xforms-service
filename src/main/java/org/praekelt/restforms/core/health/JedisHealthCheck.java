package org.praekelt.restforms.core.health;

import com.codahale.metrics.health.HealthCheck;
import org.praekelt.restforms.core.services.jedis.JedisClient;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ant cosentino
 */
public class JedisHealthCheck extends HealthCheck {
    
    private final JedisClient jedisClient;
    
    public JedisHealthCheck(JedisClient jedisClient) {
        this.jedisClient = jedisClient;
    }
    
    @Override
    protected Result check() throws Exception {
        Jedis j = this.jedisClient.borrow();
        boolean state = j.isConnected();
        this.jedisClient.revert(j);
        return state ? Result.healthy("Redis connection initialised successfully.") : Result.unhealthy("Unable to initialise connection to Redis instance.");
    }
    
}
