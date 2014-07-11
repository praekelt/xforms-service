
package org.praekelt.restforms.core;

/**
 *
 * An Exception used when an invalid value has been encountered
 * 
 * @author victorgeere
 */
public class ValueException extends Exception {

    public ValueException() {
    }

    /**
     * Constructor
     * 
     * @param message The error message
     */
    public ValueException(String message) {
        super(message);
    }
    
}
