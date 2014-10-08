package org.praekelt.restforms.core.services.jedis;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 *
 * @author ant cosentino
 */
public class JedisPoolManagerTest {
    
    public JedisPoolManagerTest() {
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
     * Test of start method, of class JedisPoolManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testStart() throws Exception {
        System.out.println("start");
        JedisPoolManager instance = new JedisPoolManager(JedisFactory.getJedisPool());
        instance.start();
    }

    /**
     * Test of stop method, of class JedisPoolManager.
     * @throws java.lang.Exception
     */
    @Test
    public void testStop() throws Exception {
        System.out.println("stop");
        new JedisFactory().build();
        JedisPoolManager instance = new JedisPoolManager(JedisFactory.getJedisPool());
        instance.stop();
    }
    
}
