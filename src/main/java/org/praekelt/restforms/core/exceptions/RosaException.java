package org.praekelt.restforms.core.exceptions;

/**
 *
 * @author ant cosentino <ant@io.co.za>
 * @since 2014-10-20
 */
public class RosaException extends Exception {

    public RosaException(String message) {
        super(message);
    }
    
    public RosaException(Throwable cause) {
        super(cause);
    }
}