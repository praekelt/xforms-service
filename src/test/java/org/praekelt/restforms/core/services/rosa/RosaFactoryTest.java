package org.praekelt.restforms.core.services.rosa;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import static org.junit.matchers.JUnitMatchers.containsString;
import org.praekelt.restforms.core.exceptions.RosaException;
import org.junit.rules.ExpectedException;

/**
 *
 * @author ant
 */
public class RosaFactoryTest {
    
    private final String form = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name>default</name><surname>default</surname><gender>default</gender><blah>123</blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";
    private final String completed = "<?xml version='1.0' ?><person><name>...</name><surname>...</surname><gender>...</gender><blah>123</blah></person>";
    
    public RosaFactoryTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Rule
    public ExpectedException e = ExpectedException.none();

    /**
     * Test of setUp method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetUp_String_boolean() throws Exception {
        System.out.println("setUp");
        assertTrue(new RosaFactory().setUp(this.form, true));
        assertFalse(new RosaFactory().setUp("", true));
        
        e.expect(RosaException.class);
        e.expectMessage(containsString("The given XML document was found to be malformed."));
        new RosaFactory().setUp("not-valid-xml", true);
    }

    /**
     * Test of setUp method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testSetUp() throws Exception {
        System.out.println("setUp");
        
        RosaFactory rosa = new RosaFactory();
        rosa.setUp(form);
        assertTrue(RosaFactory.rebuild(RosaFactory.persist(rosa)).setUp());
        assertFalse(new RosaFactory().setUp());
    }

    /**
     * Test of getQuestionTexts method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetQuestionTexts() throws Exception {
        System.out.println("getQuestionTexts");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form));
        String[] expResult = {"what's your name?","what's your surname?","what's your gender?","what's your blah?"};
        String[] result = instance.getQuestionTexts();
        assertTrue(result.length > 0);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getQuestion method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetQuestion() throws Exception {
        System.out.println("getQuestion");
        int index = 0;
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form));
        String expResult = "what's your name?";
        assertNull(instance.getQuestion(123342));
        String result = instance.getQuestion(index);
        assertFalse(result.isEmpty());
        assertEquals(expResult, result);
    }

    /**
     * Test of getCompleted method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetCompleted() throws Exception {
        System.out.println("getCompleted");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form));
        int expResult = 0;
        int result = instance.getCompleted();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTotal method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testGetTotal() throws Exception {
        System.out.println("getTotal");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form));
        int expResult = 4;
        int result = instance.getTotal();
        assertEquals(expResult, result);
    }

    /**
     * Test of processAnswer method, of class RosaFactory.
     * @throws org.praekelt.restforms.core.exceptions.RosaException
     */
    @Test
    public void testAnswerQuestion() throws Exception {
        System.out.println("answerQuestion");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form, true));
        assertTrue(instance.processAnswer("asdfas") > 0);
        assertTrue(instance.processAnswer("asdfas") > 0);
        assertTrue(instance.processAnswer("asdfas") > 0);
        
        e.expect(RosaException.class);
        e.expectMessage(containsString("Answer data-type was incorrect."));
        instance.processAnswer("abcd");
        
        
        assertTrue(instance.processAnswer("abcdefg") > 0);
    }
    
    /**
     * Test of persist method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testPersist() throws Exception {
        System.out.println("persist");
        RosaFactory instance = new RosaFactory();
        instance.setUp(this.form, true);
        byte[] stored = RosaFactory.persist(instance);
        assertTrue(stored.length > 0);
        assertTrue(stored instanceof byte[]);
    }

    /**
     * Test of rebuild method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testRebuild() throws Exception {
        System.out.println("rebuild");
        
        RosaFactory instance, rebuilt;
        instance = new RosaFactory();
        instance.setUp(this.form, true);
        instance.processAnswer("blah");
        
        byte[] stored = RosaFactory.persist(instance);
        rebuilt = RosaFactory.rebuild(stored);
        
        assertEquals(instance.getTotal(), rebuilt.getTotal());
        assertEquals(instance.getCompleted(), rebuilt.getCompleted());
        assertArrayEquals(instance.getQuestionTexts(), rebuilt.getQuestionTexts());
    }
    
    /**
     * 
     * @throws Exception 
     */
    @Test
    public void testGetCompletedXForm() throws Exception {
        System.out.println("getCompletedXForm");
        
        RosaFactory instance;
        
        instance = new RosaFactory();
        instance.setUp(this.form);
        instance.processAnswer("...");
        instance.processAnswer("...");
        instance.processAnswer("...");
        instance.processAnswer("123");
        
        assertEquals(this.completed, instance.getCompletedXForm());
    }

    /**
     * Test of processAnswer method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testProcessAnswer_String_boolean() throws Exception {
        System.out.println("processAnswer");
        RosaFactory instance = new RosaFactory();
        instance.setUp(form);
        assertEquals(1, instance.processAnswer("abc", true));
        assertEquals(2, instance.processAnswer("abc", true));
        assertEquals(3, instance.processAnswer("abc", true));
        assertEquals(-1, instance.processAnswer("123", true));
    }

    /**
     * Test of processAnswer method, of class RosaFactory.
     * @throws java.lang.Exception
     */
    @Test
    public void testProcessAnswer_String() throws Exception {
        System.out.println("processAnswer");
        RosaFactory instance = new RosaFactory();
        instance.setUp(form);
        assertEquals(1, instance.processAnswer("abc"));
        assertEquals(2, instance.processAnswer("abc"));
        assertEquals(3, instance.processAnswer("abc"));
        assertEquals(-1, instance.processAnswer("123"));
    }
}
