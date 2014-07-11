
package org.praekelt.restforms.core;

/**
 * The status returned after a question has been answered
 * 
 * @author victorgeere
 */
public class Status {
    private String type;
    private String reason;

    /**
     * Constructor
     * 
     * @param status The status message
     */
    public Status(String status) {
        this.status = status;
    }

    /**
     * Constructor
     * 
     * @param status The status message
     * @param type The status type
     */
    public Status(String status, String type) {
        this.status = status;
        this.type = type;
    }

    /**
     * 
     * 
     * @param status The status message
     * @param type The status type
     * @param reason The reason for this specific status message
     */
    public Status(String status, String type, String reason) {
        this.status = status;
        this.type = type;
        this.reason = reason;
    }

    /**
     * Getter for status message
     * 
     * @return 
     */
    public String getStatus() {
        return status;
    }

    /**
     * Setter for this status message
     * 
     * @param status 
     */
    public void setStatus(String status) {
        this.status = status;
    }
    private String status;
    
}
