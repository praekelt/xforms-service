package org.praekelt.file;

import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.InputStream;
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
public class UploadFileServiceTest {
    
    public UploadFileServiceTest() {
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
     * Test of uploadFile method, of class UploadFileService.
     */
    @Test
    public void testUploadFile() {
        System.out.println("uploadFile");
        InputStream uploadedInputStream = null;
        FormDataContentDisposition fileDetail = null;
//        UploadFileService instance = new UploadFileService();
        Response expResult = null;
//        Response result = instance.uploadFile(uploadedInputStream, fileDetail);
//        assertEquals(expResult, result);
//        TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
    }
    
}
