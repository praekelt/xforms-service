package org.praekelt.tools;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisConnectionException;

/**
 * Methods to interact with Redis via Jedis
 *
 * @author Victor
 */
public class JedisFactory {

    private JedisPool pool = null;
    static JedisFactory instance = null;

    /**
     * Instantiate a factory class
     */
    public JedisFactory() {
        Props p = new Props();
        String host = p.get("db.host"), password = p.get("db.password");
        Integer timeout = Props.getInt("db.timeout"), port = Props.getInt("db.port");
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(Props.getInt("db.poolsize"));
        pool = new JedisPool(poolConfig, host, port, timeout, password);
    }

    /**
     * Getter for pool
     *
     * @return
     */
    public JedisPool getJedisPool() {
        return pool;
    }

    /**
     * Lazy singleton constructor
     *
     * @return
     */
    public static JedisFactory getInstance() {
        if (instance == null) {
            synchronized (JedisFactory.class) {
                instance = new JedisFactory();
            }
        }
        return instance;
    }

    /**
     * Set a value in Redis
     *
     * @param key
     * @param value
     */
    public void set(String key, String value) {
        Jedis jedis = this.borrow();
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
            Logger.getLogger(JedisFactory.class.getName()).log(Level.SEVERE, null, "Could not connect to databse.");
        } catch (Exception ex) {
            Logger.getLogger(JedisFactory.class.getName()).log(Level.SEVERE, null, ex);
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
        pool.destroy();
    }

    /**
     * Borrow a resource from the pool
     *
     * @return
     */
    public Jedis borrow() {
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
    public void revert(Jedis jedis) {
        pool.returnResource(jedis);
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
            Logger.getLogger(JedisFactory.class.getName()).log(Level.SEVERE, null, "Could not connect to database");
        } catch (Exception ex) {
            Logger.getLogger(JedisFactory.class.getName()).log(Level.SEVERE, null, ex);
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
        return JedisFactory.getInstance().getKeys("*");
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
