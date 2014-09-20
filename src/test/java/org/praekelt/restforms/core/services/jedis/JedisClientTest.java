package org.praekelt.restforms.core.services.jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.praekelt.restforms.core.exceptions.JedisException;

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
        jedisFactory.setExpires(3600);
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
        assertFalse(jedisClient.keyPersist(""));
        assertFalse(jedisClient.keyPersist(null));
        assertFalse(jedisClient.keyPersist("abc"));
        assertTrue(jedisClient.keyPersist("acomplexkeyfortesting"));
    }

    /**
     * Test of keyExpire method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyExpire() throws Exception {
        System.out.println("keyExpire");
        assertEquals(false, jedisClient.keyExpire("", 0));
        assertEquals(false, jedisClient.keyExpire(null, 0));
        assertEquals(true, jedisClient.keyExpire("acomplexkeyfortesting", 60));
        assertEquals(false, jedisClient.keyExpire("acomplexkeyfortesting", 0));
    }

    /**
     * Test of keyRename method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyRename() throws Exception {
        System.out.println("keyRename");
        assertEquals(false, jedisClient.keyRename("", ""));
        assertEquals(false, jedisClient.keyRename(null, null));
        assertEquals(false, jedisClient.keyRename("", null));
        assertEquals(false, jedisClient.keyRename(null, ""));
        assertEquals(false, jedisClient.keyRename("acomplexkeyfortesting", ""));
        assertEquals(false, jedisClient.keyRename("", "anevenmorecomplexkeyfortesting"));
        assertEquals(false, jedisClient.keyRename(null, "anevenmorecomplexkeyfortesting"));
        assertEquals(false, jedisClient.keyRename("acomplexkeyfortesting", null));
        assertEquals(true, jedisClient.keyRename("acomplexkeyfortesting", "anevenmorecomplexkeyfortesting"));
    }

    /**
     * Test of keyTimeToLive method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyTimeToLive() throws Exception {
        System.out.println("keyTimeToLive");
        assertTrue(jedisClient.keyTimeToLive("acomplexkeyfortesting") > 0L);
        assertEquals(-2L, jedisClient.keyTimeToLive("xfgasdf"));
        assertEquals(-2L, jedisClient.keyTimeToLive(""));
        assertEquals(-2L, jedisClient.keyTimeToLive(null));
    }

    /**
     * Test of keyType method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyType() throws Exception {
        System.out.println("keyType");
        assertEquals("string", jedisClient.keyType("acomplexkeyfortesting"));
        assertNull(jedisClient.keyType(null));
        assertNull(jedisClient.keyType(""));
    }

    /**
     * Test of keyGet method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyGet() throws Exception {
        System.out.println("keyGet");
        assertEquals("beep boop beep", jedisClient.keyGet("acomplexkeyfortesting"));
        assertNull(jedisClient.keyGet(""));
        assertNull(jedisClient.keyGet(null));
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
        assertEquals(0L, jedisClient.keyDelete(""));
        assertEquals(0L, jedisClient.keyDelete(null));
        assertEquals(1L, jedisClient.keyDelete("acomplexkeyfortesting"));
    }

    /**
     * Test of keySet method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeySet() throws Exception {
        System.out.println("keySet");
        assertFalse(jedisClient.keySet("", ""));
        assertFalse(jedisClient.keySet(null, null));
        assertFalse(jedisClient.keySet("", null));
        assertFalse(jedisClient.keySet(null, ""));
        assertFalse(jedisClient.keySet("abc", ""));
        assertFalse(jedisClient.keySet("", "abc"));
        assertFalse(jedisClient.keySet(null, "abc"));
        assertFalse(jedisClient.keySet("abc", null));
        assertTrue(jedisClient.keySet("anothertestingkey", "somerandomvalue"));
    }

    /**
     * Test of keyExists method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testKeyExists() throws Exception {
        System.out.println("keyExists");
        assertFalse(jedisClient.keyExists(""));
        assertFalse(jedisClient.keyExists(null));
        assertTrue(jedisClient.keyExists("acomplexkeyfortesting"));
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
     * Test of hashDeleteFields method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashDeleteFields() throws Exception {
        System.out.println("hashDeleteFields");
        String[] fields = {"atestingfield"};
        String[] empty = {};
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        
        assertEquals(0L, jedisClient.hashDeleteFields(null, empty));
        assertEquals(0L, jedisClient.hashDeleteFields("", empty));
        assertEquals(0L, jedisClient.hashDeleteFields("abc", empty));
        assertEquals(0L, jedisClient.hashDeleteFields("", fields));
        assertEquals(0L, jedisClient.hashDeleteFields(null, fields));
        assertEquals(0L, jedisClient.hashDeleteFields("abc", fields));
        assertEquals(1L, jedisClient.hashDeleteFields("atestingkey", fields));
    }

    /**
     * Test of hashFieldExists method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashFieldExists() throws Exception {
        System.out.println("hashFieldExists");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        assertFalse(jedisClient.hashFieldExists("", ""));
        assertFalse(jedisClient.hashFieldExists(null, null));
        assertFalse(jedisClient.hashFieldExists("", "abc"));
        assertFalse(jedisClient.hashFieldExists("abc", ""));
        assertFalse(jedisClient.hashFieldExists(null, "abc"));
        assertFalse(jedisClient.hashFieldExists("abc", null));
        assertFalse(jedisClient.hashFieldExists("def", "ghi"));
        assertTrue(jedisClient.hashFieldExists("atestingkey", "atestingfield"));
    }

    /**
     * Test of hashGetFieldValue method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFieldValue() throws Exception {
        System.out.println("hashGetFieldValue");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        
        assertNull(jedisClient.hashGetFieldValue("", ""));
        assertNull(jedisClient.hashGetFieldValue(null, null));
        assertNull(jedisClient.hashGetFieldValue("abc", "def"));
        
        assertNull(jedisClient.hashGetFieldValue("", null));
        assertNull(jedisClient.hashGetFieldValue("", "abc"));
        
        assertNull(jedisClient.hashGetFieldValue(null, ""));
        assertNull(jedisClient.hashGetFieldValue(null, "abc"));
        
        assertNull(jedisClient.hashGetFieldValue("abc", ""));
        assertNull(jedisClient.hashGetFieldValue("abc", null));
        
        assertEquals("atestingvalue", jedisClient.hashGetFieldValue("atestingkey", "atestingfield"));
    }

    /**
     * Test of hashGetFieldsAndValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFieldsAndValues() throws Exception {
        System.out.println("hashGetFieldsAndValues");
        jedisClient.hashSetFieldValue("mykey", "myfield", "myvalue");
        assertNull(jedisClient.hashGetFieldsAndValues(null));
        assertNull(jedisClient.hashGetFieldsAndValues(""));
        assertEquals(0, jedisClient.hashGetFieldsAndValues("abc").size());
        assertEquals(1, jedisClient.hashGetFieldsAndValues("mykey").size());
    }

    /**
     * Test of hashGetFields method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFields() throws Exception {
        System.out.println("hashGetFields");
        jedisClient.hashSetFieldValue("mykey", "myfield", "myvalue");
        assertNull(jedisClient.hashGetFields(""));
        assertNull(jedisClient.hashGetFields(null));
        assertEquals(0, jedisClient.hashGetFields("abc").size());
        assertEquals(1, jedisClient.hashGetFields("mykey").size());
    }

    /**
     * Test of hashLength method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashLength() throws Exception {
        System.out.println("hashLength");
        jedisClient.hashSetFieldValue("mykey", "myfield", "myvalue");
        assertEquals(0L, jedisClient.hashLength(""));
        assertEquals(0L, jedisClient.hashLength(null));
        assertEquals(0L, jedisClient.hashLength("abc"));
        assertEquals(1L, jedisClient.hashLength("mykey"));
    }

    /**
     * Test of hashGetFieldValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetFieldValues() throws Exception {
        System.out.println("hashGetFieldValues");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        String[] empty = {};
        String[] fields = {"atestingfield"};
        assertNull(jedisClient.hashGetFieldValues("", empty));
        assertNull(jedisClient.hashGetFieldValues(null, empty));
        assertNull(jedisClient.hashGetFieldValues("", fields));
        assertNull(jedisClient.hashGetFieldValues(null, fields));
        assertNull(jedisClient.hashGetFieldValues("abc", empty));
        assertEquals(1, jedisClient.hashGetFieldValues("abc", fields).size());
        assertEquals(1, jedisClient.hashGetFieldValues("atestingkey", fields).size());
    }

    /**
     * Test of hashSetFieldsAndValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashSetFieldsAndValues() throws Exception {
        System.out.println("hashSetFieldsAndValues");
        jedisClient.hashSetFieldValue("atestingkey", "atestingfield", "atestingvalue");
        Map<String, String> map = new HashMap<String, String>(5);
        Map<String, String> empty = new HashMap<String, String>(5);
        map.put("anothertestingfield", "anothertestingvalue");
        assertFalse(jedisClient.hashSetFieldsAndValues(null, map));
        assertFalse(jedisClient.hashSetFieldsAndValues("", map));
        assertFalse(jedisClient.hashSetFieldsAndValues(null, empty));
        assertFalse(jedisClient.hashSetFieldsAndValues("", empty));
        assertFalse(jedisClient.hashSetFieldsAndValues("abc", empty));
        assertTrue(jedisClient.hashSetFieldsAndValues("atestingkey", map));
    }

    /**
     * Test of hashSetFieldValueIfNotExists method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashSetFieldValueIfNotExists() throws Exception {
        System.out.println("hashSetFieldValueIfNotExists");
        jedisClient.hashSetFieldValue("mykey", "myfield", "myvalue");
        assertFalse(jedisClient.hashSetFieldValueIfNotExists("mykey", "myfield", "myother"));
        assertTrue(jedisClient.hashSetFieldValueIfNotExists("mykey", "myotherfield", "myothervalue"));
    }

    /**
     * Test of hashGetValues method, of class JedisClient.
     * @throws java.lang.Exception
     */
    @Test
    public void testHashGetValues() throws Exception {
        System.out.println("hashGetValues");
        jedisClient.hashSetFieldValue("mykey", "myfield", "myvalue");
        assertNull(jedisClient.hashGetValues(""));
        assertNull(jedisClient.hashGetValues(null));
        assertEquals(1, jedisClient.hashGetValues("mykey").size());
        assertEquals(0, jedisClient.hashGetValues("abc").size());
    }
    
    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testHashSetPOJO() throws Exception {
        String key = "blah";
        byte[] objectBuffer = "lkjasdfkljasdfa".getBytes();
        assertFalse(jedisClient.hashSetPOJO(key, new byte[0]));
        assertFalse(jedisClient.hashSetPOJO(key, null));
        assertTrue(jedisClient.hashSetPOJO(key, objectBuffer));
    }
    
    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testHashGetPOJO() throws Exception {
        String key = "mykey";
        byte[] objectBuffer = "lkjasdfkljasdfa".getBytes();
        assertTrue(jedisClient.hashSetPOJO(key, objectBuffer));
        byte[] expResult = "lkjasdfkljasdfa".getBytes();
        byte[] result = jedisClient.hashGetPOJO(key);
        assertTrue(result instanceof byte[]);
        assertArrayEquals(expResult, result);
        assertNull(jedisClient.hashGetPOJO(null));
        assertNull(jedisClient.hashGetPOJO(""));
    }

    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testHashPOJOExists() throws Exception {
        String key = "mykey";
        assertTrue(jedisClient.hashSetPOJO(key, new byte[1]));
        assertTrue(jedisClient.hashPOJOExists(key));
        assertFalse(jedisClient.hashPOJOExists(null));
        assertFalse(jedisClient.hashPOJOExists(""));
    }

    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testHashDeletePOJO() throws Exception {
        String key = "mykey";
        assertTrue(jedisClient.hashSetPOJO(key, new byte[1]));
        assertTrue(jedisClient.hashPOJOExists(key));
        assertTrue(jedisClient.hashDeletePOJO(key));
        assertFalse(jedisClient.hashDeletePOJO(key));
        assertFalse(jedisClient.hashPOJOExists(key));
    }
}
