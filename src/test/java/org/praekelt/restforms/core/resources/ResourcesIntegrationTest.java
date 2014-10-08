package org.praekelt.restforms.core.resources;

import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.praekelt.restforms.core.resources.ResponsesResource.ResponsesRepresentation;
import org.praekelt.restforms.core.services.jedis.JedisClient;
import org.praekelt.restforms.core.services.jedis.JedisFactory;

/**
 * @author ant cosentino
 */
public class ResourcesIntegrationTest {

    private static JedisClient jedis;
    private static FormsResource forms;
    private static AnswersResource answers;
    private static ResponsesResource responses;
    private static final ResponsesRepresentation rr = new ResponsesRepresentation("123");
    private static final String xform = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name></name><surname></surname><gender></gender><blah></blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";

    @BeforeClass
    public static void setUpClass() {
        JedisFactory jedisFactory = new JedisFactory();

        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisFactory.setExpires(3600);
        jedis = jedisFactory.build();

        forms = new FormsResource(jedis);
        answers = new AnswersResource(jedis);
        responses = new ResponsesResource(jedis);
    }

    @AfterClass
    public static void tearDownClass() throws Exception {

        if (jedis.keyDeleteAll() < 1L) {
            fail("Couldn't remove all keys from Reids.");
        }
    }

    @Before
    public void setUp() {}

    @After
    public void tearDown() {}

    @Test
    public void testXFormResources() throws Exception {
        System.out.println("XFormResources");
        
        Response createdForm = forms.create(xform);
        String formId = jedis.keyGetAll().iterator().next(),
               createdFormJson = "{\"id\":\""+formId+"\",\"status\":201,\"message\":\"Created XForm.\"}",
               firstQuestionJson = "{\"id\":\""+formId+"\",\"question\":\"what's your surname?\",\"status\":200,\"message\":\"Question completed.\"}",
               secondQuestionJson = "{\"id\":\""+formId+"\",\"question\":\"what's your gender?\",\"status\":200,\"message\":\"Question completed.\"}",
               thirdQuestionJson = "{\"id\":\""+formId+"\",\"question\":\"what's your blah?\",\"status\":200,\"message\":\"Question completed.\"}",
               fourthQuestionJson = "{\"id\":\""+formId+"\",\"status\":200,\"message\":\"XForm completed.\"}",
               completedFormXml = "<?xml version='1.0' ?><person><name>123</name><surname>123</surname><gender>123</gender><blah>123</blah></person>";
        
        Response firstQuestion = responses.answerQuestion(formId, rr),
                 secondQuestion = responses.answerQuestion(formId, rr),
                 thirdQuestion = responses.answerQuestion(formId, rr),
                 fourthQuestion = responses.answerQuestion(formId, rr),
                 completedForm = answers.getSingle(formId);
        
        assertEquals(201, createdForm.getStatus());
        assertEquals(createdFormJson, createdForm.getEntity());
        
        assertEquals(200, firstQuestion.getStatus());
        assertEquals(firstQuestionJson, firstQuestion.getEntity());
        
        assertEquals(200, secondQuestion.getStatus());
        assertEquals(secondQuestionJson, secondQuestion.getEntity());
        
        assertEquals(200, thirdQuestion.getStatus());
        assertEquals(thirdQuestionJson, thirdQuestion.getEntity());
        
        assertEquals(200, fourthQuestion.getStatus());
        assertEquals(fourthQuestionJson, fourthQuestion.getEntity());
        
        assertEquals(200, completedForm.getStatus());
        assertEquals(completedFormXml, completedForm.getEntity());
    }
}
