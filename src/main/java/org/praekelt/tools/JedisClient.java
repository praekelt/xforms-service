package org.praekelt.tools;

import static java.lang.System.exit;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class JedisClient {

    private static final Logger logger = Logger.getLogger(JedisClient.class.getName());
    private final JedisPool pool;

    public JedisClient(JedisPool pool) {
        this.pool = pool;
    }

    /**
     * Set a value in Redis
     *
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        Jedis jedis = this.borrow();
        if (jedis == null) {
            logger.log(Level.SEVERE, null, new NullPointerException("Could not retrieve Redis connection."));
            exit(-1);
        }
        try {
            jedis.set(key, value);
        } finally {
            revert(jedis);
        }
    }

    /**
     * Return a value from Redis
     *
     * @param key
     * @return
     */
    public String get(String key) {
        Jedis jedis = borrow();
        String value = "";
        try {
            value = jedis.get(key);
        } catch (NullPointerException ex) {
            logger.log(Level.SEVERE, null, "Could not connect to database.");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            revert(jedis);
        }
        return value;
    }

    /**
     * Destroy the pool
     *
     * Unset the pool
     */
    public void destroy() {
        this.pool.destroy();
    }

    /**
     * Borrow a resource from the pool
     *
     * @return
     */
    public Jedis borrow() {
        Jedis resource = null;
        try {
            resource = this.pool.getResource();
        } catch (JedisConnectionException jdce) {
            logger.log(Level.SEVERE, null, jdce);
        }
        return resource;
    }

    /**
     * Return a resource to the pool
     *
     * @param jedis
     */
    public void revert(Jedis jedis) {
        this.pool.returnResource(jedis);
    }

    /**
     * Get a Set of String keys from the database
     *
     * @param key
     * @return
     */
    public Set<String> getKeys(String key) {
        Set<String> keys = null;
        Jedis jedis = borrow();
        try {
            keys = jedis.keys(key);
        } catch (NullPointerException nex) {
            logger.log(Level.SEVERE, null, "Could not connect to database");
        } catch (Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {
            revert(jedis);
        }
        return keys;
    }

    /**
     * Get all available keys "*" as a String Set
     *
     * @return
     */
    public Set<String> getKeys() {
        return this.getKeys("*");
    }

    /**
     *
     */
    public void deleteAll() {
        Jedis jedis = borrow();
        try {
            Set<String> keys = jedis.keys("*");
            for (String key : keys) {
                jedis.del(key);
            }
        } finally {
            revert(jedis);
        }
    }

    /**
     * Remove a key from the database
     *
     * @param key
     */
    public void delete(String key) {
        Jedis jedis = borrow();
        if (jedis != null) {
            try {
                jedis.del(key);
            } finally {
                revert(jedis);
            }
        }
    }

}
