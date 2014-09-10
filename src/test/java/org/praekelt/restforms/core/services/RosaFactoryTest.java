/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package org.praekelt.restforms.core.services;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ant
 */
public class RosaFactoryTest {
    
    private final String form = "<h:html xmlns=\"http://www.w3.org/2002/xforms\" xmlns:h=\"http://www.w3.org/1999/xhtml\" xmlns:ev=\"http://www.w3.org/2001/xml-events\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:jr=\"http://openrosa.org/javarosa\"><h:head><h:title>xforms service form</h:title><model><instance><person><name>default</name><surname>default</surname><gender>default</gender><blah>123</blah></person></instance><bind nodeset=\"name\" type=\"string\" /><bind nodeset=\"surname\" type=\"string\" /><bind nodeset=\"gender\" type=\"string\" /><bind nodeset=\"blah\" type=\"int\" /></model></h:head><h:body><input ref=\"name\"><label>what's your name?</label></input><input ref=\"surname\"><label>what's your surname?</label></input><input ref=\"gender\"><label>what's your gender?</label></input><input ref=\"blah\"><label>what's your blah?</label></input></h:body></h:html>";
    
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

    /**
     * Test of setUp method, of class RosaFactory.
     */
    @Test
    public void testSetUp_String_boolean() {
        System.out.println("setUp");
        assertTrue(new RosaFactory().setUp(this.form, true));
        assertFalse(new RosaFactory().setUp("", true));
    }

    /**
     * Test of setUp method, of class RosaFactory.
     */
    @Test
    public void testSetUp() {
        System.out.println("setUp");
        assertFalse(new RosaFactory().setUp());
    }

    /**
     * Test of getQuestionTexts method, of class RosaFactory.
     */
    @Test
    public void testGetQuestionTexts() {
        System.out.println("getQuestionTexts");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form, true));
        String[] expResult = {"what's your name?","what's your surname?","what's your gender?","what's your blah?"};
        String[] result = instance.getQuestionTexts();
        assertTrue(result.length > 0);
        assertArrayEquals(expResult, result);
    }

    /**
     * Test of getQuestion method, of class RosaFactory.
     */
    @Test
    public void testGetQuestion() {
        System.out.println("getQuestion");
        int index = 0;
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form, true));
        String expResult = "what's your name?";
        assertNull(instance.getQuestion(123342));
        String result = instance.getQuestion(index);
        assertFalse(result.isEmpty());
        assertEquals(expResult, result);
    }

    /**
     * Test of getCompleted method, of class RosaFactory.
     */
    @Test
    public void testGetCompleted() {
        System.out.println("getCompleted");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form, true));
        int expResult = 0;
        int result = instance.getCompleted();
        assertEquals(expResult, result);
    }

    /**
     * Test of getTotal method, of class RosaFactory.
     */
    @Test
    public void testGetTotal() {
        System.out.println("getTotal");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form, true));
        int expResult = 4;
        int result = instance.getTotal();
        assertEquals(expResult, result);
    }

    /**
     * Test of answerQuestion method, of class RosaFactory.
     */
    @Test
    public void testAnswerQuestion() {
        System.out.println("answerQuestion");
        RosaFactory instance = new RosaFactory();
        assertTrue(instance.setUp(this.form, true));
        assertTrue(instance.answerQuestion("asdfas", -1));
        assertTrue(instance.answerQuestion("asdfas", -1));
        assertTrue(instance.answerQuestion("asdfas", -1));
        assertTrue(instance.answerQuestion(1234, 3));
        assertFalse(instance.answerQuestion("asdfas", 23445));
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
        instance.answerQuestion("blah", -1);
        
        byte[] stored = RosaFactory.persist(instance);
        rebuilt = RosaFactory.rebuild(stored);
        rebuilt.setUp();
        
        assertEquals(instance.getTotal(), rebuilt.getTotal());
        assertEquals(instance.getCompleted(), rebuilt.getCompleted());
        assertArrayEquals(instance.getQuestionTexts(), rebuilt.getQuestionTexts());
    }
}
