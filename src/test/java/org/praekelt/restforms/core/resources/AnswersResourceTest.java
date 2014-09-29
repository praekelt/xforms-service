package org.praekelt.restforms.core.resources;

import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import org.praekelt.restforms.core.services.jedis.JedisClient;
import org.praekelt.restforms.core.services.jedis.JedisFactory;
import org.praekelt.restforms.core.services.rosa.RosaFactory;

/**
 *
 * @author ant cosentino
 */
public class AnswersResourceTest {
    
    private static JedisClient jedisClient;
    private AnswersResource instance;
    private AnswersResource exceptionInstance;
    private String instanceId;
    private String exceptionInstanceId;
    private static final String form = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name></name><surname></surname><gender></gender><blah></blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";
    private static final String completed = "<?xml version='1.0' ?><person><name>123</name><surname>123</surname><gender>123</gender><blah>123</blah></person>";
    
    public AnswersResourceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() throws Exception {
        JedisFactory jedisFactory = new JedisFactory();
        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisFactory.setExpires(3600);
        jedisClient = jedisFactory.build();
    }
    
    @Before
    public void setUp() throws Exception {
        instance = new AnswersResource(jedisClient);
        exceptionInstance = new AnswersResource(jedisClient);
        
        if (instance == null) {
            fail("Couldnt initialise AnswersResource for testing.");
        }
        
        String completedForm;
        instanceId = instance.createResource("form", form);
        exceptionInstanceId = exceptionInstance.createResource("form", form);
        
        if (instanceId == null || instanceId.isEmpty()) {
            fail("Couldn't add xForm template to Redis.");
        }
        
        if (exceptionInstanceId == null || exceptionInstanceId.isEmpty()) {
            fail("Couldn't add xForm template to Redis.");
        }
        
        if (!exceptionInstance.updateResource(exceptionInstanceId, "lkjadsflkjasf".getBytes())) {
            fail("Couldn't add fake serialised RosaFactory to Redis");
        }
        
        RosaFactory rf = new RosaFactory();
        
        if (!rf.setUp(form)) {
            fail("Couldn't initialise RosaFactory instance.");
        }
        
        while (rf.answerQuestion("123") != -1) {}
        
        completedForm = rf.getCompletedXForm();
        
        if (completedForm == null || completedForm.isEmpty()) {
            fail("Couldn't serialise the completed xForm.");
        }
        
        if (!instance.updateResource(instanceId, "completed", completedForm)) {
            fail("Couldn't add completed xForm to Redis.");
        }
        
        if (!instance.updateResource(instanceId, RosaFactory.persist(rf))) {
            fail("Couldn't add serialised RosaFactory to Redis.");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        
        if (jedisClient.keyDeleteAll() < 1L) {
            fail("Couldn't remove all keys from Redis storage.");
        }
    }
    
    @Rule
    public ExpectedException e = ExpectedException.none();
    
    /**
     * Test of getSingle method, of class AnswersResource.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetSingle() throws Exception {
        System.out.println("getSingle");
        
        RosaFactory r = new RosaFactory();
        String badRequest = "{\"status\":400,\"message\":\"The XForm associated with the given ID is not ready for output. Please complete all questions first.\"}",
               notFound = "{\"status\":404,\"message\":\"No XForm was found to be associated with the given ID. This could also indicate server error.\"}",
               id = instance.createResource("form", form);
        
        assertTrue(r.setUp(form));
        assertTrue(r.answerQuestion("d") > 0);
        assertTrue(r.answerQuestion("e") > 0);
        assertTrue(r.answerQuestion("f") > 0);
        assertTrue(instance.updateResource(id, RosaFactory.persist(r)));
        
        Response instanceIdResponse = instance.getSingle(instanceId);
        
        assertEquals(completed, instanceIdResponse.getEntity());
        assertEquals(badRequest, instance.getSingle(id).getEntity());
        assertEquals(notFound, instance.getSingle("blah").getEntity());
    }
}