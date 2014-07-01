
package org.praekelt.xforms;

/**
 *
 * @author victorgeere
 */
public class Status {
    private String type;
    private String reason;

    public Status(String status) {
        this.status = status;
    }

    public Status(String status, String type) {
        this.status = status;
        this.type = type;
    }

    public Status(String status, String type, String reason) {
        this.status = status;
        this.type = type;
        this.reason = reason;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    private String status;
    
}
