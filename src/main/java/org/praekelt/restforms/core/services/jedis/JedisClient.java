package org.praekelt.restforms.core.services.jedis;

import com.codahale.metrics.health.HealthCheck;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import org.praekelt.restforms.core.exceptions.JedisException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * 
 * @author victor geere
 * @author ant cosentino
 * @author simon kelly
 */
public final class JedisClient {

    private static final Logger logger = Logger.getLogger(JedisFactory.class.getName());
    private final JedisPool pool;
    
    static class JedisHealthCheck extends HealthCheck {
        
        private final JedisClient jedisClient;

        public JedisHealthCheck(JedisClient jedisClient) {
            this.jedisClient = jedisClient;
        }
        
        @Override
        protected Result check() throws Exception {
            Jedis j = this.jedisClient.borrow();
            boolean state = j.isConnected();
            this.jedisClient.revert(j);
            return state ? Result.healthy("A connection to Redis was established.") : Result.unhealthy("A connection to Redis was not established.");
        }
    }

    JedisClient(JedisPool pool) {
        this.pool = pool;
    }
    
    /**
     * Borrow a resource from the pool
     * 
     * unfortunately, we have to make this method
     * public in order to implement a healthcheck
     * for the redis instance...
     * 
     * @return
     */
    private Jedis borrow() {
        Jedis resource = null;
        try {
            resource = pool.getResource();
        } catch (JedisConnectionException jdce) {
        }
        return resource;
    }

    /**
     * Return a resource to the pool
     * 
     * unfortunately, we have to make this method
     * public in order to implement a healthcheck
     * for the redis instance...
     * 
     * @param jedis
     */
    private void revert(Jedis jedis) {
        pool.returnResource(jedis);
    }
    
    /**
     * 
     * @param dynamic type
     * @param action
     * @return dynamic type
     * @throws JedisException 
     */
    private <T> T execute(JedisAction<T> action) throws JedisException {
        Jedis jedis = this.borrow();
        
        try {
            return action.execute(jedis);
        } catch (Exception e) {
            
            try {
                return action.handleException(e);
            } catch (Exception ee) {
                throw new JedisException(ee);
            }
        } finally {
            this.revert(jedis);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="redis key methods">
    public boolean keyPersist(final String key) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.persist(key) == 1;
            }
        });
    }
    
    public boolean keyExpire(final String key, final int seconds) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.expire(key, seconds) == 1;
            }
        });
    }
    
    public boolean keyRename(final String oldKey, final String newKey) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.rename(oldKey, newKey).equals("OK");
            }
        });
    }
    
    /**
     * 
     * Returns the remaining time to live of a key that has a timeout.
     * This introspection capability allows a Redis client to check 
     * how many seconds a given key will continue to be part of the dataset.
     * In Redis 2.6 or older the command returns -1 if the key does not 
     * exist or if the key exist but has no associated expire.
     * 
     * Starting with Redis 2.8 the return value in case of error changed:
     *  - The command returns -2 if the key does not exist.
     *  - The command returns -1 if the key exists but has no associated expire.
     * 
     * @param key
     * @return
     * @throws JedisException 
     */
    public long keyTimeToLive(final String key) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.ttl(key);
            }
        });
    }
    
    public String keyType(final String key) throws JedisException {
        return this.execute(new JedisAction<String>() {
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.type(key);
            }
        });
    }
    
    /**
     * Return a value from Redis
     *
     * @param key
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public String keyGet(final String key) throws JedisException {
        return this.execute(new JedisAction<String>() {
            
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.get(key);
            }
        });
    }
    
    /**
     * Get a Set of String keysByPattern from the database
     *
     * @param pattern
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public Set<String> keysByPattern(final String pattern) throws JedisException {
        return this.execute(new JedisAction<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) throws Exception {
                return jedis.keys(pattern);
            }
        });
    }

    /**
     * Get all available keysByPattern "*" as a String Set
     *
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public Set<String> keyGetAll() throws JedisException {
        return this.keysByPattern("*");
    }
    
    /**
     * nuke all keysByPattern within the instance
     * 
     * @throws JedisException 
     */
    public void keyDeleteAll() throws JedisException {
        
        final String[] keys = (String[]) this.keyGetAll().toArray();
        
        this.execute(new JedisAction<Void>() {
            @Override
            public Void execute(Jedis jedis) {
                jedis.del(keys);
                return null;
            }
        });
    }

    /**
     * Remove a key from the database
     *
     * @param key
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public void keyDelete(final String key) throws JedisException {
        
        this.execute(new JedisAction<Void>() {

            @Override
            public Void execute(Jedis jedis) throws Exception {
                jedis.del(key);
                return null;
            }
        });
    }

    /**
     * 
     * @param key
     * @param value
     * @throws JedisException 
     */
    public void keySet(final String key, final String value) throws JedisException {
        
        this.execute(new JedisAction<Void>() {
            
            @Override
            public Void execute(Jedis jedis) throws Exception {
                jedis.set(key, value);
                return null;
            }
        });
    }
    
    public boolean keyExists(final String key) throws JedisException {
        
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.exists(key);
            }
        });
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="redis hash methods">
    public long hashDeleteFields(final String key, final String... fields) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hdel(key, fields);
            }
        });
    }
    
    public boolean hashFieldExists(final String key, final String field) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.hexists(key, field);
            }
        });
    }
    
    public String hashGetFieldValue(final String key, final String field) throws JedisException {
        return this.execute(new JedisAction<String>() {
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.hget(key, field);
            }
        });
    }
    
    public Map<String, String> hashGetFieldsAndValues(final String key) throws JedisException {
        return this.execute(new JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> execute(Jedis jedis) throws Exception {
                return jedis.hgetAll(key);
            }
        });
    }
    
    public long hashIncrementValueBy(final String key, final String field, final long value) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hincrBy(key, field, value);
            }
        });
    }
    
    public double hashIncrementValueByFloat(final String key, final String field, final double value) throws JedisException {
        return this.execute(new JedisAction<Double>() {
            @Override
            public Double execute(Jedis jedis) throws Exception {
                return jedis.hincrByFloat(key, field, value);
            }
        });
    }
    
    public Set<String> hashGetFields(final String key) throws JedisException {
        return this.execute(new JedisAction<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) throws Exception {
                return jedis.hkeys(key);
            }
        });
    }
    
    public long hashLength(final String key) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hlen(key);
            }
        });
    }
    
    public List<String> hashGetFieldValues(final String key, final String... fields) throws JedisException {
        return this.execute(new JedisAction<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) throws Exception {
                return jedis.hmget(key, fields);
            }
        });
    }
    
    public void hashSetFieldsAndValues(final String key, final Map<String, String> map) throws JedisException {
        this.execute(new JedisAction<Void>() {
            @Override
            public Void execute(Jedis jedis) throws Exception {
                jedis.hmset(key, map);
                return null;
            }
        });
    }
    
    public long hashSetFieldValue(final String key, final String field, final String value) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hset(key, field, value);
            }
        });
    }
    
    public long hashSetFieldValueIfNotExists(final String key, final String field, final String value) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hsetnx(key, field, value);
            }
        });
    }
    
    public List<String> hashGetValues(final String key) throws JedisException {
        return this.execute(new JedisAction<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) throws Exception {
                return jedis.hvals(key);
            }
        });
    }
    // </editor-fold>
}
