package org.praekelt.restforms.core.resources;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.gson.JsonSyntaxException;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import static org.junit.matchers.JUnitMatchers.containsString;
import org.junit.rules.ExpectedException;
import org.praekelt.restforms.core.services.jedis.JedisFactory;

/**
 *
 * @author ant cosentino
 */
public class BaseResourceTest {
    
    private static BaseResource instance;
    private final TestRepresentation rep = new TestRepresentation("abc", 123);
    
    private static class TestRepresentation {
        
        private final String field1;
        private final int field2;
        
        @JsonCreator
        public TestRepresentation(
            @JsonProperty("field1") String field1,
            @JsonProperty("field2") int field2
        ) {
            this.field1 = field1;
            this.field2 = field2;
        }
        
        @JsonProperty("field1")
        public String getField1() { return field1; }
        
        @JsonProperty("field2")
        public int getField2() { return field2; }
    }
    
    public BaseResourceTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() throws Exception {
        JedisFactory jedisFactory = new JedisFactory();
        jedisFactory.setHost("127.0.0.1");
        jedisFactory.setPoolSize(5);
        jedisFactory.setPort(6379);
        jedisFactory.setTimeout(500);
        jedisFactory.setExpires(3600);
        instance = new BaseResource(jedisFactory.build()) {};
        
        if (instance == null) {
            fail("Couldn't initialise BaseResource instance for testing.");
        }
        HashMap<String, String> map = new HashMap<String, String>(10);
        map.put("field1", "value1");
        map.put("field2", "value2");
        
        if (!BaseResource.jedis.hashSetFieldsAndValues("test-key", map)) {
            fail("Couldn't create test string hash values in Redis.");
        }
        
        if (!BaseResource.jedis.hashSetPOJO("test-key", "value3".getBytes())) {
            fail("Couldn't create test byte[] hash value in Redis.");
        }
    }
    
    @After
    public void tearDown() throws Exception {
        if (BaseResource.jedis.keyDeleteAll() < 1L) {
            fail("Couldn't remove all keys from Redis.");
        }
    }
    
    @Rule
    public ExpectedException e = ExpectedException.none();

    /**
     * Test of toJson method, of class BaseResource.
     */
    @Test
    public void testToJson() {
        System.out.println("toJson");
        String expected = "{\"status\":123,\"message\":\"abc\"}";
        String result = BaseResource.toJson(new BaseResource.BaseResponse(123, "abc"), BaseResource.BaseResponse.class);
        
        assertEquals(expected, result);
        assertNull(BaseResource.toJson(null, BaseResource.BaseResponse.class));
        assertNull(BaseResource.toJson(new BaseResource.BaseResponse(123, "abc"), null));
        assertNull(BaseResource.toJson(null, null));
    }

    /**
     * Test of fromJson method, of class BaseResource.
     */
    @Test
    public void testFromJson() {
        System.out.println("fromJson");
        TestRepresentation t = (TestRepresentation) BaseResource.fromJson("{\"field1\":\"abc\",\"field2\":\"123\"}", TestRepresentation.class);
        
        assertEquals(rep.getField1(), t.getField1());
        assertEquals(rep.getField2(), t.getField2());
        
        assertNull(BaseResource.fromJson(null, null));
        assertNull(BaseResource.fromJson("", null));
        assertNull(BaseResource.fromJson(null, TestRepresentation.class));
        assertNull(BaseResource.fromJson("", TestRepresentation.class));
        assertNull(BaseResource.fromJson("{ \"i'm a little teapot\": true }", null));
        
        e.expect(JsonSyntaxException.class);
        e.expectMessage(containsString("Expected BEGIN_OBJECT"));
        BaseResource.fromJson("let's just pretend this is json", TestRepresentation.class);
    }

    /**
     * Test of verifyResource method, of class BaseResource.
     */
    @Test
    public void testVerifyResource() {
        System.out.println("verifyResource");
        
        assertTrue(instance.verifyResource("test-key"));
        
        assertFalse(instance.verifyResource("something-else"));
        assertFalse(instance.verifyResource(""));
        assertFalse(instance.verifyResource(null));
    }

    /**
     * Test of verifyField method, of class BaseResource.
     */
    @Test
    public void testVerifyField() {
        System.out.println("verifyField");
        
        assertTrue(instance.verifyField("test-key", "field1"));
        
        assertFalse(instance.verifyField("test-key", "not-a-field"));
        assertFalse(instance.verifyField("", ""));
        assertFalse(instance.verifyField(null, null));
        assertFalse(instance.verifyField("", null));
        assertFalse(instance.verifyField(null, ""));
    }

    /**
     * Test of fetchField method, of class BaseResource.
     */
    @Test
    public void testFetchField() {
        System.out.println("fetchField");
        String result = instance.fetchField("test-key", "field1");
        
        assertEquals("value1", result);
        
        assertNull(instance.fetchField("abc", "123"));
        assertNull(instance.fetchField("", ""));
        assertNull(instance.fetchField("abc", null));
        assertNull(instance.fetchField(null, "123"));
    }

    /**
     * Test of fetchResource method, of class BaseResource.
     */
    @Test
    public void testFetchResource() {
        System.out.println("fetchResource");
        Map<String, String> result = instance.fetchResource("test-key");
        
        assertEquals("value1", result.get("field1"));
        assertEquals("value2", result.get("field2"));
        
        assertNull(instance.fetchResource("not-a-key"));
        assertNull(instance.fetchResource(null));
        assertNull(instance.fetchResource(""));
    }

    /**
     * Test of fetchPOJO method, of class BaseResource.
     */
    @Test
    public void testFetchPOJO() {
        System.out.println("fetchPOJO");
        byte[] result = instance.fetchPOJO("test-key");
        
        assertArrayEquals("value3".getBytes(), result);
        
        assertNull(instance.fetchPOJO("not-a-key"));
        assertNull(instance.fetchPOJO(""));
        assertNull(instance.fetchPOJO(null));
    }

    /**
     * Test of createResource method, of class BaseResource.
     */
    @Test
    public void testCreateResource() {
        System.out.println("createResource");
        String result = instance.createResource("another-field", "another-value");
        
        assertFalse(result.isEmpty());
        assertFalse(instance.fetchField(result, "another-field").isEmpty());
        
        assertNull(instance.createResource("", ""));
        assertNull(instance.createResource(null, null));
        assertNull(instance.createResource("", null));
        assertNull(instance.createResource(null, ""));
    }

    /**
     * Test of updateResource method, of class BaseResource.
     */
    @Test
    public void testUpdateResource_3args() {
        System.out.println("updateResource");
        String newValue = "new-value1";
        
        assertTrue(instance.updateResource("test-key", "field1", newValue));
        assertEquals(newValue, instance.fetchField("test-key", "field1"));
        
        assertFalse(instance.updateResource("not-a-key", "not-a-field", "not-a-value"));
        assertFalse(instance.updateResource("", "", ""));
        assertFalse(instance.updateResource(null, null, null));
    }

    /**
     * Test of updateResource method, of class BaseResource.
     */
    @Test
    public void testUpdateResource_String_byteArr() {
        System.out.println("updateResource");
        byte[] newValue = "new-value3".getBytes();
        
        assertTrue(instance.updateResource("test-key", newValue));
        assertArrayEquals(newValue, instance.fetchPOJO("test-key"));
        
        assertFalse(instance.updateResource("not-a-key", "new-value3".getBytes()));
        assertFalse(instance.updateResource("", "".getBytes()));
        assertFalse(instance.updateResource(null, null));
    }
}
