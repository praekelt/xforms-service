package org.praekelt.models;
import java.util.Date;
import java.util.UUID;
 
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
 
public class Form {
 
    private String id = UUID.randomUUID().toString();
 
    @NotBlank
    private String name;
 
    public Form() {
    }
 
    public Form(String name) {
        super();
        this.name = name;
    }
 
    public String getId() {
        return id;
    }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}