package org.praekelt.restforms.core.services.jedis;

import java.util.Set;
import java.util.logging.Logger;
import org.praekelt.restforms.core.exceptions.JedisException;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * 
 * @author ant cosentino
 * @author simon kelly
 */
public class JedisClient {

    private static final Logger logger = Logger.getLogger(JedisFactory.class.getName());
    private final JedisPool pool;

    public JedisClient(JedisPool pool) {
        this.pool = pool;
    }
    
    /**
     * Borrow a resource from the pool
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
     * @param jedis
     */
    private void revert(Jedis jedis) {
        pool.returnResource(jedis);
    }
    
    /**
     * 
     * @param <T>
     * @param action
     * @return
     * @throws JedisException 
     */
    private <T> T execute(RedisAction<T> action) throws JedisException {
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
    
    /**
     * Return a value from Redis
     *
     * @param key
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public String get(final String key) throws JedisException {
//        Jedis jedis = borrow();
//        String value = "";
//        try {
//            value = jedis.get(key);
//        } catch (NullPointerException ex) {
//            logger.log(Level.SEVERE, null, "Could not connect to databse.");
//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, null, ex);
//        } finally {
//            revert(jedis);
//        }
//        return value;
        
        return this.execute(new RedisAction<String>() {
            
            @Override
            public String execute(Jedis jedis) throws Exception {
                return jedis.get(key);
            }
        });
    }
    
    /**
     * Get a Set of String keys from the database
     *
     * @param key
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public Set<String> getKeys(final String key) throws JedisException {
//        Set<String> keys = null;
//        Jedis jedis = borrow();
//        try {
//            keys = jedis.keys(key);
//        } catch (NullPointerException nex) {
//            logger.log(Level.SEVERE, null, "Could not connect to database");
//        } catch (Exception ex) {
//            logger.log(Level.SEVERE, null, ex);
//        } finally {
//            revert(jedis);
//        }
//        return keys;
        return this.execute(new RedisAction<Set<String>>() {

            @Override
            public Set<String> execute(Jedis jedis) throws Exception {
                return jedis.keys(key);
            }
        });
    }

    /**
     * Get all available keys "*" as a String Set
     *
     * @return
     * @throws org.praekelt.restforms.core.exceptions.JedisException
     */
    public Set<String> getKeys() throws JedisException {
        return this.getKeys("*");
    }
    
    /**
     * nuke all keys within the instance
     * 
     * @throws JedisException 
     */
    public void deleteAll() throws JedisException {
//        Jedis jedis = borrow();
//        try {
//            Set<String> keys = jedis.keys("*");
//            for (String key : keys) {
//                jedis.del(key);
//            }
//        } finally {
//            revert(jedis);
//        }
        
        final String[] keys = (String[]) this.getKeys().toArray();
        
        this.execute(new RedisAction<Void>() {
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
    public void delete(final String key) throws JedisException {
//        Jedis jedis = borrow();
//        if (jedis != null) {
//            try {
//                jedis.del(key);
//            } finally {
//                revert(jedis);
//            }
//        }
        
        this.execute(new RedisAction<Void>() {

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
    public void set(final String key, final String value) throws JedisException {
        
        //    /**
        //     * Set a value in Redis
        //     *
        //     * @param key
        //     * @param value
        //     */
        //    public void set(String key, String value) {
        //        Jedis jedis = this.borrow();
        //        try {
        //            jedis.set(key, value);
        //        } finally {
        //            revert(jedis);
        //        }
        //    }
        this.execute(new RedisAction<Void>() {
            
            @Override
            public Void execute(Jedis jedis) throws Exception {
                jedis.set(key, value);
                return null;
            }
        });
    }
    
    public boolean exists(final String key) throws JedisException {
        return this.execute(new RedisAction<Boolean>() {
            @Override
            public Boolean execute(Jedis jedis) throws Exception {
                return jedis.exists(key);
            }
        });
    }
}
