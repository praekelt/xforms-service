package org.praekelt.restforms.core.exceptions;

/**
 *
 * @author ant cosentino
 */
public class RosaException extends Exception {

    public RosaException(String message) {
        super(message);
    }
    
    public RosaException(Throwable cause) {
        super(cause);
    }
}