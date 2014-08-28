package org.praekelt.tools;

import org.praekelt.restforms.core.services.Props;
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
public class PropsTest {
    
    public PropsTest() {
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
     * Test of get method, of class Props.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String key = "";
        Props instance = new Props();
        String expResult = "";
        String result = instance.get(key);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getInt method, of class Props.
     */
    @Test
    public void testGetInt() {
        System.out.println("getInt");
        String key = "";
        Integer expResult = null;
//        Integer result = Props.getInt(key);
//        assertEquals(expResult, result);
//        TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
