package org.praekelt.restforms.core.resources;

import java.util.HashMap;
import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.praekelt.restforms.core.resources.BaseResource.fromJson;
import static org.praekelt.restforms.core.resources.BaseResource.jedis;
import org.praekelt.restforms.core.resources.FormsResource.FormsResponse;
import org.praekelt.restforms.core.services.jedis.JedisFactory;

/**
 *
 * @author ant cosentino
 */
public class FormsResourceTest {
    
    private static FormsResource instance;
    private static FormsResource noJedisInstance;
    private final String form = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name></name><surname></surname><gender></gender><blah></blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";
    
    public FormsResourceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        JedisFactory jedisFactory = new JedisFactory();
        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisFactory.setExpires(3600);
        instance = new FormsResource(jedisFactory.build());
        noJedisInstance = new FormsResource(null);
        
        if (instance == null || noJedisInstance == null) {
            fail("Couldn't initialise one of the FormsResource instances for testing.");
        }
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
        HashMap<String, String> map = new HashMap<String, String>(10);
        map.put("field1", "value1");
        map.put("field2", "value2");
        map.put("form", form);
        
        if (!jedis.hashSetFieldsAndValues("test-key", map)) {
            fail("Couldn't create test hash values in Redis.");
        }
        
        if (!jedis.hashSetPOJO("test-key", "value3".getBytes())) {
            fail("Couldn't create test byte[] hash values in Redis.");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        
        if (jedis.keyDeleteAll() < 1L) {
            fail("Couldn't remove all keys from Redis storage.");
        }
    }

    /**
     * Test of create method, of class FormsResource.
     */
    @Test
    public void testCreate() {
        System.out.println("create");
        
        Response result = instance.create(form);
        FormsResponse fr = (FormsResponse) fromJson(result.getEntity().toString(), FormsResource.responseEntity);
        
        String id = fr.getId(),
               created = "{\"id\":\""+id+"\",\"status\":201,\"message\":\"Created XForm.\"}",
               internalServerError = "{\"status\":500,\"message\":\"An error occurred while attempting to save the provided XForm. Please ensure the XML you provided is well-formed and valid.\"}",
               badRequest = "{\"status\":400,\"message\":\"No XML payload was provided in the request.\"}";
        
        assertEquals(created, result.getEntity());
        assertEquals(internalServerError, instance.create("abcdefg").getEntity());
        assertEquals(badRequest, instance.create("").getEntity());
        assertEquals(internalServerError, noJedisInstance.create("abcdefg").getEntity());
    }

    /**
     * Test of getSingle method, of class FormsResource.
     */
    @Test
    public void testGetSingle() {
        System.out.println("getSingle");
        
        String notFound = "{\"status\":404,\"message\":\"No XForm was found associated with the given ID.\"}";
        
        assertEquals(form, instance.getSingle("test-key").getEntity());
        assertEquals(notFound, instance.getSingle("not-a-key").getEntity());
    }
}