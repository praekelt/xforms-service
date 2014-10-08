package org.praekelt.restforms.core.services.jedis;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import redis.clients.jedis.JedisPool;

/**
 *
 * @author ant
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
        assertTrue(JedisFactory.getJedisPool() instanceof JedisPool);
    }

    /**
     * Test of setHost method, of class JedisFactory.
     */
    @Test
    public void testSetHost() {
        System.out.println("setHost");
        String host = "localhost";
        JedisFactory instance = new JedisFactory();
        instance.setHost(host);
        
        assertEquals(instance.getHost(), host);
    }

    /**
     * Test of setPort method, of class JedisFactory.
     */
    @Test
    public void testSetPort() {
        System.out.println("setPort");
        int port = 5432;
        JedisFactory instance = new JedisFactory();
        instance.setPort(port);
        
        assertEquals(instance.getPort(), port);
    }

    /**
     * Test of setPassword method, of class JedisFactory.
     */
    @Test
    public void testSetPassword() {
        System.out.println("setPassword");
        String password = "abc";
        JedisFactory instance = new JedisFactory();
        instance.setPassword(password);
        
        assertEquals(instance.getPassword(), password);
    }

    /**
     * Test of setTimeout method, of class JedisFactory.
     */
    @Test
    public void testSetTimeout() {
        System.out.println("setTimeout");
        int timeout = 1234;
        JedisFactory instance = new JedisFactory();
        instance.setTimeout(timeout);
        
        assertEquals(instance.getTimeout(), timeout);
    }

    /**
     * Test of setPoolSize method, of class JedisFactory.
     */
    @Test
    public void testSetPoolSize() {
        System.out.println("setPoolSize");
        int poolSize = 0;
        JedisFactory instance = new JedisFactory();
        instance.setPoolSize(poolSize);
        
        assertEquals(instance.getPoolSize(), poolSize);
    }
    
    /**
     * Test of setExpires method, of class JedisFactory.
     */
    @Test
    public void testSetExpires() {
        System.out.println("setExpires");
        int expires = 3600;
        JedisFactory instance = new JedisFactory();
        instance.setExpires(expires);
        
        assertEquals(instance.getExpires(), expires);
    }

    /**
     * Test of build method, of class JedisFactory.
     */
    @Test
    public void testBuild() {
        System.out.println("build");
        
        JedisFactory instance = new JedisFactory();
        instance.setHost("localhost");
        instance.setPassword("abcd");
        instance.setPort(6379);
        instance.setTimeout(100);
        instance.setPoolSize(5);
        instance.setExpires(3600);
        
        JedisClient result = instance.build();
        
        assertNotNull(result);
    }
}
