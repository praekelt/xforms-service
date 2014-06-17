package org.praekelt.tools;

import java.util.Set;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 *
 * @author Victor
 */
public class JedisFactoryTest {
    
    public JedisFactoryTest() {
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
     * Test of getJedisPool method, of class JedisFactory.
     */
    @Test
    public void testGetJedisPool() {
        System.out.println("getJedisPool");
        JedisFactory instance = new JedisFactory();
        JedisPool expResult = null;
        JedisPool result = instance.getJedisPool();
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getInstance method, of class JedisFactory.
     */
    @Test
    public void testGetInstance() {
        System.out.println("getInstance");
        JedisFactory expResult = null;
        JedisFactory result = JedisFactory.getInstance();
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of set method, of class JedisFactory.
     */
    @Test
    public void testSet() {
        System.out.println("set");
        String key = "";
        String value = "";
        JedisFactory instance = JedisFactory.getInstance();
        
//        instance.set(key, value);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of get method, of class JedisFactory.
     */
    @Test
    public void testGet() {
        System.out.println("get");
        String key = "";
        JedisFactory instance = new JedisFactory();
        String expResult = "";
        String result = instance.get(key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of destroy method, of class JedisFactory.
     */
    @Test
    public void testDestroy() {
        System.out.println("destroy");
        JedisFactory instance = new JedisFactory();
        instance.destroy();
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of borrow method, of class JedisFactory.
     */
    @Test
    public void testBorrow() {
        System.out.println("borrow");
        JedisFactory instance = new JedisFactory();
        Jedis expResult = null;
        Jedis result = instance.borrow();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of revert method, of class JedisFactory.
     */
    @Test
    public void testRevert() {
        System.out.println("revert");
        Jedis jedis = null;
        JedisFactory instance = new JedisFactory();
        instance.revert(jedis);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeys method, of class JedisFactory.
     */
    @Test
    public void testGetKeys_String() {
        System.out.println("getKeys");
        String key = "";
        JedisFactory instance = new JedisFactory();
        Set<String> expResult = null;
        Set<String> result = instance.getKeys(key);
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of getKeys method, of class JedisFactory.
     */
    @Test
    public void testGetKeys_0args() {
        System.out.println("getKeys");
        JedisFactory instance = new JedisFactory();
        Set<String> expResult = null;
        Set<String> result = instance.getKeys();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of deleteAll method, of class JedisFactory.
     */
    @Test
    public void testDeleteAll() {
        System.out.println("deleteAll");
        JedisFactory instance = new JedisFactory();
//        instance.deleteAll();
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }

    /**
     * Test of delete method, of class JedisFactory.
     */
    @Test
    public void testDelete() {
        System.out.println("delete");
        String key = "";
        JedisFactory instance = new JedisFactory();
        instance.delete(key);
        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
