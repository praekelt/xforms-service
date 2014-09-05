package org.praekelt.restforms.core.services.jedis;

import com.codahale.metrics.health.HealthCheck;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    
    private final JedisPool pool;
    
    JedisClient(JedisPool pool) {
        this.pool = pool;
    }
    
    public static final class JedisHealthCheck extends HealthCheck {
        
        private final JedisClient jedisClient;

        public JedisHealthCheck(JedisClient jedisClient) {
            this.jedisClient = jedisClient;
        }
        
        @Override
        protected Result check() throws Exception {
            Jedis j = jedisClient.borrow();
            boolean state = j.isConnected();
            jedisClient.yield(j);
            return state ? Result.healthy("A connection to Redis was established.") : Result.unhealthy("A connection to Redis was not established.");
        }
    }
    
    /**
     * Borrow a resource from the pool
     * 
     * @return
     */
    private Jedis borrow() {
        return pool.getResource();
    }

    /**
     * Return a resource to the pool
     * 
     * @param jedis
     */
    private void yield(Jedis jedis) {
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
            this.yield(jedis);
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
     * @return 
     * @throws JedisException 
     */
    public long keyDeleteAll() throws JedisException {
        
        final Object[] keys = this.keyGetAll().toArray();
        
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) {
                long l = 0L;
                
                for (Object key : keys) {
                    l += jedis.del(key.toString());
                }
                return l;
            }
        });
    }

    /**
     * Remove a key from the database
     *
     * @param key
     * @return 
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public long keyDelete(final String key) throws JedisException {
        return this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.del(key);
            }
        });
    }

    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws JedisException 
     */
    public boolean keySet(final String key, final String value) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.set(key, value).equals("OK");
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
    
    public boolean hashSetFieldsAndValues(final String key, final Map<String, String> map) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.hmset(key, map).equals("OK");
            }
        });
    }
    
    public boolean hashSetFieldValue(final String key, final String field, final String value) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                long set = jedis.hset(key, field, value);
                return set == 1 || set == 0;
            }
        });
    }
    
    public boolean hashSetFieldValueIfNotExists(final String key, final String field, final String value) throws JedisException {
        return this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.hsetnx(key, field, value) == 1;
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
