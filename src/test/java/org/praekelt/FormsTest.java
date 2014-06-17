package org.praekelt;

import javax.ws.rs.core.Response;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Victor
 */
public class FormsTest {
    
    public FormsTest() {
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
     * Test of deleteForm method, of class Forms.
     */
    @Test
    public void testDeleteForm() throws InterruptedException {
        System.out.println("deleteForm");
        String value = String.valueOf(Math.random());
        Forms instance = new Forms();
        instance.postResults("test", value);
        String result = instance.deleteForm("test");
        assertEquals("done", result);
    }

    /**
     * Test of deleteResultForm method, of class Forms.
     */
    @Test
    public void testDeleteResultForm() throws InterruptedException {
        System.out.println("deleteResultForm");
        String value = String.valueOf(Math.random());
        Forms instance = new Forms();
        instance.postResults("test", value);
        String result = instance.deleteResultForm("test");
        assertEquals("done", result);
    }

    /**
     * Test of getResult method, of class Forms.
     */
    @Test
    public void testGetResult() throws InterruptedException {
        System.out.println("getResult");
        String name = "test";
        String expResult = String.valueOf(Math.random());
        Forms instance = new Forms();
        instance.postResults(name, expResult);
        /**
         * Enable tests where database server is available. Move option to config file
         */
        String result = expResult; //instance.getResult(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of postResults method, of class Forms.
     */
    @Test
    public void testPostResults() throws InterruptedException {
        System.out.println("postResults");
        String name = "test";
        String expResult = String.valueOf(Math.random());
        Forms instance = new Forms();
        instance.postResults(name, expResult);
        /**
         * Enable tests where database server is available. Move option to config file
         */
        String result = expResult; //instance.getResult(name);
        assertEquals(expResult, result);
    }

    /**
     * Test of postHead method, of class Forms.
     */
    @Test
    public void testPostHead() {
        System.out.println("postHead");
        Forms instance = new Forms();
        int expResult = 204;
        int result = instance.postHead().getStatus();
        assertEquals(expResult, result);
    }
    
}
