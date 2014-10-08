/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.praekelt.restforms.core.services.jedis;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.ExpectedException;
import redis.clients.jedis.Jedis;

/**
 *
 * @author ant
 */
public class JedisActionTest {
    
    public JedisActionTest() {
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
     * Test of handleException method, of class JedisAction.
     * @throws java.lang.Exception
     */
    @Test
    public void testHandleException() throws Exception {
        System.out.println("handleException");
        
        JedisAction instance = new JedisAction() {
            @Override
            public Object execute(Jedis jedis) throws Exception {
                return null;
            }
        };
        
        e.expect(Exception.class);
        instance.handleException(new Exception());
    }
}
