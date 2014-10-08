package org.praekelt.restforms.core.services;

import org.praekelt.restforms.core.services.rosa.RosaFactory;
import static org.junit.Assert.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.praekelt.restforms.core.services.jedis.JedisClient;
import org.praekelt.restforms.core.services.jedis.JedisFactory;

/**
 *
 * @author ant cosentino
 */
public class RosaFactoryJedisClientIntegrationTest {
	
    private static RosaFactory serialiseRosa;
    private static RosaFactory unserialiseRosa;
    private static JedisClient jedisInstance;
    private static final String form = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name></name><surname></surname><gender></gender><blah></blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";

    @BeforeClass
    public static void setUpClass() {
        JedisFactory jedisFactory = new JedisFactory();
        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisFactory.setExpires(3600);
        jedisInstance = jedisFactory.build();
        
        if (jedisInstance == null) {
            fail("Couldn't initialise JedisClient instance for Redis operations.");
        }
    }

    @AfterClass
    public static void tearDownClass() {}

    @Before
    public void setUp() throws Exception {
        serialiseRosa = new RosaFactory();
        
        if (serialiseRosa == null) {
            fail("Couldn't initialise RosaFactory instance.");
        }
        
        if (!serialiseRosa.setUp(form)) {
            fail("RosaFactory instance could not process xForm.");
        }
        
        if (!jedisInstance.hashSetFieldValue("a-key", "form", form)) {
            fail("Couldn't create a new hash with test values.");
        }
    }

    @After
    public void tearDown() throws Exception {
        
        if (jedisInstance.keyDeleteAll() < 1L) {
            fail("Couldn't remove keys from Redis.");
        }
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void testSerialiseAndSetPOJO() throws Exception {
        System.out.println("serialiseAndSetPOJO");
        byte[] serialised;
        String key = "a-key";

        serialised = RosaFactory.persist(serialiseRosa);
        assertNotNull(serialised);
        assertTrue(serialised.length > 0);

        assertTrue(jedisInstance.hashSetPOJO(key, serialised));
        assertTrue(jedisInstance.hashPOJOExists(key));
    }

    /**
     * @throws java.lang.Exception
     */
    @Test
    public void testGetPOJOAndUnserialise() throws Exception {
        System.out.println("getPOJOAndUnserialise");
        int completed;
        byte[] serialised, stored;
        String key;

        assertTrue(serialiseRosa.processAnswer("test") > 0);
        completed = serialiseRosa.getCompleted();

        serialised = RosaFactory.persist(serialiseRosa);
        assertNotNull(serialised);
        assertTrue(serialised.length > 0);

        key = "a-key";
        assertTrue(jedisInstance.hashSetPOJO(key, serialised));
        assertTrue(jedisInstance.hashPOJOExists(key));

        stored = jedisInstance.hashGetPOJO(key);
        assertNotNull(stored);
        assertTrue(stored.length > 0);
        assertArrayEquals(stored, serialised);

        unserialiseRosa = RosaFactory.rebuild(stored);
        assertNotNull(unserialiseRosa);
        assertTrue(unserialiseRosa.setUp());
        assertEquals(completed, unserialiseRosa.getCompleted());

        assertTrue(jedisInstance.hashDeletePOJO(key));
        assertFalse(jedisInstance.hashPOJOExists(key));
    }
}