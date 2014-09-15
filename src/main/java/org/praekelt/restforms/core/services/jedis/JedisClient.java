package org.praekelt.restforms.core.services.jedis;

import com.codahale.metrics.health.HealthCheck;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.praekelt.restforms.core.exceptions.JedisException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * 
 * @author victor geere
 * @author ant cosentino
 * @author simon kelly
 */
public final class JedisClient {
    
    private static final byte[] OBJECT_FIELD = "object".getBytes();
    private final int expires;
    private final JedisPool pool;
    
    JedisClient(JedisPool pool, int expires) {
        this.pool = pool;
        this.expires = expires;
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
            long dbSize = j.dbSize();
            String info = StringUtils.join(StringUtils.split(j.info(), "\r\n"), " - ");
            jedisClient.yield(j);
            
            return state ? Result.healthy("A connection to Redis was established. Connection information: " + info + " - dbsize:" + dbSize) : Result.unhealthy("A connection to Redis was not established.");
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
        
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.persist(key) == 1;
            }
        }) : false;
    }
    
    public boolean keyExpire(final String key, final int seconds) throws JedisException {
        boolean safe = (key != null && !"".equals(key)) && seconds > 0;
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.expire(key, seconds) == 1;
            }
        }) : false;
    }
    
    public boolean keyRename(final String oldKey, final String newKey) throws JedisException {
        boolean safe = oldKey != null && !"".equals(oldKey) && (newKey != null && !"".equals(newKey));
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.rename(oldKey, newKey).equals("OK");
            }
        }) : false;
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
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.ttl(key);
            }
        }) : -2L;
    }
    
    public String keyType(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<String>() {
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.type(key);
            }
        }) : null;
    }
    
    /**
     * Return a value from Redis
     *
     * @param key
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public String keyGet(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<String>() {
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.get(key);
            }
        }) : null;
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
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.del(key);
            }
        }) : 0L;
    }

    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws JedisException 
     */
    public boolean keySet(final String key, final String value) throws JedisException {
        boolean safe = (key != null && !"".equals(key) && (value != null && !"".equals(value)));
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                boolean created = jedis.set(key, value).equals("OK");
                if (created) {
                    jedis.expire(key, expires);
                    return created;
                }
                return !created;
            }   
        }) : false;
    }
    
    public boolean keyExists(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.exists(key);
            }
        }) : false;
    }
    // </editor-fold>
    
    // <editor-fold defaultstate="collapsed" desc="redis hash methods">
    public boolean hashSetPOJO(String key, final byte[] objectBuffer) throws JedisException {
	
	if (key != null && !"".equals(key) && objectBuffer != null && objectBuffer.length > 0) {
            final byte[] byteKey = key.getBytes();
            return this.execute(new JedisAction<Boolean>() {
                @Override
                public Boolean execute(Jedis jedis) throws Exception {
                    long hset = jedis.hset(byteKey, OBJECT_FIELD, objectBuffer);
                    return hset == 1L || hset == 0L;
                }
            });
	}
	return false;
    }

    public byte[] hashGetPOJO(String key) throws JedisException {

        if (key != null && !"".equals(key)) {
            final byte[] byteKey = key.getBytes();
            return this.execute(new JedisAction<byte[]>() {
                @Override
                public byte[] execute(Jedis jedis) throws Exception {
                    return jedis.hget(byteKey, OBJECT_FIELD);
                }
            });
        }
        return null;
    }

    public boolean hashPOJOExists(String key) throws JedisException {

        if (key != null && !"".equals(key)) {
            final byte[] byteKey = key.getBytes();
            return this.execute(new JedisAction<Boolean>() {
                @Override
                public Boolean execute(Jedis jedis) {
                    return jedis.hexists(byteKey, OBJECT_FIELD);
                }
            });
        }
        return false;
    }

    public boolean hashDeletePOJO(String key) throws JedisException {

        if (key != null && !"".equals(key)) {
            final byte[] byteKey = key.getBytes();
            final byte[][] byteFields = {OBJECT_FIELD};
            return this.execute(new JedisAction<Boolean>() {
                @Override
                public Boolean execute(Jedis jedis) throws Exception {
                    long l;
                    l = jedis.hdel(byteKey, byteFields);
                    return l == 1L;
                }
            });
        }
        return false;
    }
    
    public long hashDeleteFields(final String key, final String... fields) throws JedisException {
        boolean safe = (key != null && !"".equals(key) && fields.length > 0);
        return safe ? this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hdel(key, fields);
            }
        }) : 0L;
    }
    
    public boolean hashFieldExists(final String key, final String field) throws JedisException {
        boolean safe = (key != null && !"".equals(key) && (field != null && !"".equals(field)));
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.hexists(key, field);
            }
        }) : false;
    }
    
    public String hashGetFieldValue(final String key, final String field) throws JedisException {
        boolean safe = (key != null && !"".equals(key) && (field != null && !"".equals(field)));
        return safe ? this.execute(new JedisAction<String>() {
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.hget(key, field);
            }
        }) : null;
    }
    
    public Map<String, String> hashGetFieldsAndValues(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Map<String, String>>() {
            @Override
            public Map<String, String> execute(Jedis jedis) throws Exception {
                return jedis.hgetAll(key);
            }
        }) : null;
    }
    
    public Set<String> hashGetFields(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Set<String>>() {
            @Override
            public Set<String> execute(Jedis jedis) throws Exception {
                return jedis.hkeys(key);
            }
        }) : null;
    }
    
    public long hashLength(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<Long>() {
            @Override
            public Long execute(Jedis jedis) throws Exception {
                return jedis.hlen(key);
            }
        }) : 0L;
    }
    
    public List<String> hashGetFieldValues(final String key, final String... fields) throws JedisException {
        boolean safe = (key != null && !"".equals(key)) && fields.length > 0; 
        return safe ? this.execute(new JedisAction<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) throws Exception {
                return jedis.hmget(key, fields);
            }
        }) : null;
    }
    
    public boolean hashSetFieldsAndValues(final String key, final Map<String, String> map) throws JedisException {
        boolean safe = (key != null && !"".equals(key)) && map.size() > 0;
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                boolean created = jedis.hmset(key, map).equals("OK");
                
                if (created) {
                    jedis.expire(key, expires);
                    return created;
                }
                return !created;
            }
        }) : false;
    }
    
    public boolean hashSetFieldValue(final String key, final String field, final String value) throws JedisException {
        boolean safe = (key != null && !"".equals(key)) 
            && (field != null && !"".equals(field)) 
            && (value != null && !"".equals(value));
        
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                long hset = jedis.hset(key, field, value);
                return hset == 1L || hset == 0L;
            }
        }) : false;
    }
    
    public boolean hashSetFieldValueIfNotExists(final String key, final String field, final String value) throws JedisException {
        boolean safe = (key != null && !"".equals(key)) 
            && (field != null && !"".equals(field)) 
            && (value != null && !"".equals(value));
        
        return safe ? this.execute(new JedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.hsetnx(key, field, value) == 1L;
            }
        }) : false;
    }
    
    public List<String> hashGetValues(final String key) throws JedisException {
        return (key != null && !"".equals(key)) ? this.execute(new JedisAction<List<String>>() {
            @Override
            public List<String> execute(Jedis jedis) throws Exception {
                return jedis.hvals(key);
            }
        }) : null;
    }
    // </editor-fold>
}
