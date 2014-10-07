package org.praekelt.restforms.core.resources;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.praekelt.restforms.core.resources.BaseResource.jedis;
import org.praekelt.restforms.core.services.jedis.JedisFactory;
import org.praekelt.restforms.core.services.rosa.RosaFactory;

/**
 *
 * @author ant cosentino
 */
public class ResponsesResourceTest {

    private static ResponsesResource instance;
    private static String id;
    private static final String form = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name></name><surname></surname><gender></gender><blah></blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";
    
    public ResponsesResourceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        JedisFactory jedisFactory = new JedisFactory();
        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisFactory.setExpires(3600);
        instance = new ResponsesResource(jedisFactory.build());
        
        if (instance == null) {
            fail("Couldn't initialise ResponsesResource instance for testing.");
        }
    }
    
    @AfterClass
    public static void tearDownClass() {}
    
    @Before
    public void setUp() throws Exception {
        id = instance.createResource("form", form);
        
        if (id == null) {
            fail("Couldn't store the testing XForm in Redis.");
        }
        
        RosaFactory r = new RosaFactory();
        
        if (!r.setUp(form)) {
            fail("Couldn't initialise RosaFactory instance.");
        }
        
        byte[] serialised = RosaFactory.persist(r);
        
        if (!instance.updateResource(id, serialised)) {
            fail("Couldn't save serialised RosaFactory instance in Redis.");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        
        if (jedis.keyDeleteAll() < 1L) {
            fail("Couldn't remove all keys from Redis storage.");
        }
    }

    /**
     * Test of answerQuestion method, of class ResponsesResource.
     */
    @Test
    public void testAnswerQuestion() {
        System.out.println("answerQuestion");
        
        String ok1 = "{\"id\":\"" + id + "\",\"status\":200,\"message\":\"XForm completed.\"}";
        String ok2 = "{\"id\":\"" + id + "\",\"question\":\"what's your surname?\",\"status\":200,\"message\":\"Question completed.\"}";
        String ok3 = "{\"id\":\"" + id + "\",\"question\":\"what's your gender?\",\"status\":200,\"message\":\"Question completed.\"}";
        String ok4 = "{\"id\":\"" + id + "\",\"question\":\"what's your blah?\",\"status\":200,\"message\":\"Question completed.\"}";
        String notFound = "{\"status\":404,\"message\":\"No XForm was found associated with the given ID.\"}";
        String badRequest = "{\"status\":400,\"message\":\"No `answer` field was provided in the request payload.\"}";
        
        assertEquals(badRequest, instance.answerQuestion(id, new ResponsesResource.ResponsesRepresentation(null, 0)).getEntity());
        assertEquals(notFound, instance.answerQuestion("abcdefg", new ResponsesResource.ResponsesRepresentation("abc", 0)).getEntity());
        assertEquals(ok2, instance.answerQuestion(id, new ResponsesResource.ResponsesRepresentation("abc", 0)).getEntity());
        assertEquals(ok3, instance.answerQuestion(id, new ResponsesResource.ResponsesRepresentation("abc", 1)).getEntity());
        assertEquals(ok4, instance.answerQuestion(id, new ResponsesResource.ResponsesRepresentation("abc", 2)).getEntity());
        assertEquals(ok1, instance.answerQuestion(id, new ResponsesResource.ResponsesRepresentation("123", 3)).getEntity());
    }

    /**
     * Test of getQuestion method, of class ResponsesResource.
     */
    @Test
    public void testGetQuestion() {
        System.out.println("getQuestion");
        
        String ok = "{\"id\":\"" + id + "\",\"question\":\"what's your name?\",\"status\":200,\"message\":\"Question retrieved successfully.\"}";
        String badRequest = "{\"status\":400,\"message\":\"The question you requested was out of bounds. Please try again.\"}";
        String notFound = "{\"status\":404,\"message\":\"No XForm was found associated with the given ID.\"}";
        
        assertEquals(ok, instance.getQuestion(id, new ResponsesResource.ResponsesRepresentation(0)).getEntity());
        assertEquals(badRequest, instance.getQuestion(id, new ResponsesResource.ResponsesRepresentation(10)).getEntity());
        assertEquals(notFound, instance.getQuestion("abcdefg", new ResponsesResource.ResponsesRepresentation(0)).getEntity());
    }
}
