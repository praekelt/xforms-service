package org.praekelt.restforms.core.services.jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.praekelt.restforms.core.exceptions.JedisException;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 *
 * @author ant cosentino
 */
public class JedisClientTest {
    
    private static JedisClient jedisClient;
    
    public JedisClientTest() {}
    
    @BeforeClass
    public static void setUpClass() {
        JedisFactory jedisFactory = new JedisFactory();
        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisClient = jedisFactory.build();
    }
    
    @AfterClass
    public static void tearDownClass() {}
    
    @Before
    public void setUp() {
        try {
            jedisClient.keySet("acomplexkeyfortesting", "beep boop beep");
        } catch (JedisException e) {
            System.err.println("Couldn't set up.");
        }
    }
    
    @After
    public void tearDown() {
        try {
            jedisClient.keyDeleteAll();
        } catch (JedisException e) {
            System.err.println("Couldn't tear down...");
        }
    }

    /**
     * Test of keyPersist method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyPersist() throws Exception {
        System.out.println("keyPersist");
        String key = "acomplexkeyfortesting";
        boolean expResult = false;
        boolean result = jedisClient.keyPersist(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of keyExpire method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyExpire() throws Exception {
        System.out.println("keyExpire");
        String key = "";
        int seconds = 0;
        boolean expResult = false;
        boolean result = jedisClient.keyExpire(key, seconds);
        assertEquals(expResult, result);
    }

    /**
     * Test of keyRename method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyRename() throws Exception {
        System.out.println("keyRename");
        String oldKey = "acomplexkeyfortesting";
        String newKey = "anevenmorecomplexkeyfortesting";
        boolean expResult = true;
        boolean result = jedisClient.keyRename(oldKey, newKey);
        assertEquals(expResult, result);
    }

    /**
     * Test of keyTimeToLive method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyTimeToLive() throws Exception {
        System.out.println("keyTimeToLive");
        String key = "acomplexkeyfortesting";
        long expResult = -1L;
        long result = jedisClient.keyTimeToLive(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of keyType method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyType() throws Exception {
        System.out.println("keyType");
        String key = "acomplexkeyfortesting";
        String expResult = "string";
        String result = jedisClient.keyType(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of keyGet method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyGet() throws Exception {
        System.out.println("keyGet");
        String key = "acomplexkeyfortesting";
        String expResult = "beep boop beep";
        String result = jedisClient.keyGet(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of keysByPattern method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeysByPattern() throws Exception {
        System.out.println("keysByPattern");
        String pattern = "acomplexkeyfortesting";
        Set<String> result = jedisClient.keysByPattern(pattern);
        assertEquals(1, result.size());
    }

    /**
     * Test of keyGetAll method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyGetAll() throws Exception {
        System.out.println("keyGetAll");
        Set<String> result = jedisClient.keyGetAll();
        assertEquals(1, result.size());
    }
    
    /**
     * Test of keyDelete method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyDelete() throws Exception {
        System.out.println("keyDelete");
        String key = "";
        long deleted = jedisClient.keyDelete(key);
        assertEquals(deleted, 0L);
    }
    
    /**
     * Test of keyDeleteAll method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyDeleteAll() throws Exception {
        System.out.println("keyDeleteAll");
        Set<String> keys = jedisClient.keyGetAll();
        long deleted = jedisClient.keyDeleteAll();
        assertEquals(keys.size(), deleted);
    }

    /**
     * Test of keySet method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeySet() throws Exception {
        System.out.println("keySet");
        String key = "anothertestingkey";
        String value = "somerandomvalue";
        boolean result = jedisClient.keySet(key, value);
        assertEquals(result, true);
    }

    /**
     * Test of keyExists method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyExists() throws Exception {
        System.out.println("keyExists");
        String key = "";
        boolean expResult = false;
        boolean result = jedisClient.keyExists(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashDeleteFields method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashDeleteFields() throws Exception {
        System.out.println("hashDeleteFields");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        String key = "atestingkey";
        String[] fields = {"atestingfield"};
        long expResult = 1L;
        long result = jedisClient.hashDeleteFields(key, fields);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashFieldExists method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashFieldExists() throws Exception {
        System.out.println("hashFieldExists");
        String key = "";
        String field = "";
        boolean expResult = false;
        boolean result = jedisClient.hashFieldExists(key, field);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashGetFieldValue method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFieldValue() throws Exception {
        System.out.println("hashGetFieldValue");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        String key = "atestingkey";
        String field = "atestingfield";
        String expResult = "atestingvalue";
        String result = jedisClient.hashGetFieldValue(key, field);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashGetFieldsAndValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFieldsAndValues() throws Exception {
        System.out.println("hashGetFieldsAndValues");
        jedisClient.hashSetFieldValue("mykey", "myfield", "myvalue");
        String key = "mykey";
        Map<String, String> result = jedisClient.hashGetFieldsAndValues(key);
        assertEquals(result.size(), 1);
    }

    /**
     * Test of hashIncrementValueBy method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashIncrementValueBy() throws Exception {
        System.out.println("hashIncrementValueBy");
        String key = "";
        String field = "";
        long value = 0L;
        long result = jedisClient.hashIncrementValueBy(key, field, value);
        assertEquals(result, value);
    }

    /**
     * Test of hashGetFields method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFields() throws Exception {
        System.out.println("hashGetFields");
        String key = "";
        Set<String> result = jedisClient.hashGetFields(key);
        assertEquals(result.size(), 0);
    }

    /**
     * Test of hashLength method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashLength() throws Exception {
        System.out.println("hashLength");
        String key = "";
        long expResult = 0L;
        long result = jedisClient.hashLength(key);
        assertEquals(expResult, result);
    }

    /**
     * Test of hashGetFieldValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFieldValues() throws Exception {
        System.out.println("hashGetFieldValues");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        String key = "atestingkey";
        String[] fields = {"atestingfield"};
        List<String> result = jedisClient.hashGetFieldValues(key, fields);
        assertEquals(result.size(), 1);
    }

    /**
     * Test of hashSetFieldsAndValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashSetFieldsAndValues() throws Exception {
        System.out.println("hashSetFieldsAndValues");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        String key = "atestingkey";
        Map<String, String> map = new HashMap<String, String>();
        map.put("anothertestingfield", "anothertestingvalue");
        boolean created = jedisClient.hashSetFieldsAndValues(key, map);
        assertEquals(created, true);
    }

    /**
     * Test of hashSetFieldValue method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashSetFieldValue() throws Exception {
        System.out.println("hashSetFieldValue");
        String key = "mykey";
        String field = "myfield";
        String value = "myvalue";
        boolean result = jedisClient.hashSetFieldValue(key, field, value);
        assertEquals(true, result);
    }

    /**
     * Test of hashSetFieldValueIfNotExists method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashSetFieldValueIfNotExists() throws Exception {
        System.out.println("hashSetFieldValueIfNotExists");
        String key = "atestingkey";
        String field = "atestingfield";
        String value = "atestingvalue";
        boolean result = jedisClient.hashSetFieldValueIfNotExists(key, field, value);
        assertEquals(true, result);
    }

    /**
     * Test of hashGetValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetValues() throws Exception {
        System.out.println("hashGetValues");
        String key = "";
        List<String> result = jedisClient.hashGetValues(key);
        assertEquals(0, result.size());
    }
}
