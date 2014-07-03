
package org.praekelt.xforms;

import java.io.IOException;

/**
 * Used when form serialization fails
 * 
 * @author victorgeere
 */
public class SerializationException extends Exception {

    public SerializationException(String message, IOException ex) {
        super(message, ex);
    }
    
}
