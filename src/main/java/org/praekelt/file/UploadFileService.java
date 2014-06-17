package org.praekelt.file;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.praekelt.tools.JedisFactory;

/**
 * REST Service to upload empty forms
 *
 */
@Path("/forms")
public class UploadFileService {

    /**
     * Read uploaded file from the inputStream
     * 
     * @param uploadedInputStream
     * @param fileDetail
     * @return 
     */
    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response uploadFile(
            @FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail) {

        if (1 == 2) {
            writeToFile(uploadedInputStream, fileDetail.getFileName());
        } else {
            writeToRedis(uploadedInputStream, fileDetail.getFileName());
        }

        String output = "File uploaded as : " + fileDetail.getFileName();

        return Response.status(200).entity(output).build();

    }

    /**
     * Save uploaded form to Redis
     * 
     * @param uploadedInputStream
     * @param fileKey 
     */
    private void writeToRedis(InputStream uploadedInputStream, String fileKey) {
        try {
            int read = 0;
            byte[] bytes = new byte[1024];
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            //System.out.println("File uploaded : " + out.toString());
            JedisFactory.getInstance().set(fileKey, out.toString());

        } catch (IOException ex) {
            Logger.getLogger(UploadFileService.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Save uploaded file to disk
     * 
     * @param uploadedInputStream
     * @param uploadedFileLocation 
     */
    private void writeToFile(InputStream uploadedInputStream,
            String uploadedFileLocation) {

        try {
            OutputStream out = new FileOutputStream(new File(
                    uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {

            e.printStackTrace();
        }

    }

}
