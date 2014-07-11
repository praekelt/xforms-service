package org.praekelt.restforms;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;
import org.praekelt.tools.JedisFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.Configuration;
 
public class RestformsConfiguration extends Configuration {
	@NotEmpty
    private String template;

    @NotEmpty
    private String defaultName = "Stranger";

    @JsonProperty
    public String getTemplate() {
        return template;
    }

    @JsonProperty
    public void setTemplate(String template) {
        this.template = template;
    }

    @JsonProperty
    public String getDefaultName() {
        return defaultName;
    }

    @JsonProperty
    public void setDefaultName(String name) {
        this.defaultName = name;
    }
    
    @Valid
    @NotNull
    private JedisFactory jedisFactory = new JedisFactory();

    @JsonProperty("jedisFactory")
    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }

    @JsonProperty("jedisFactory")
    public void setJedisFactory(JedisFactory factory) {
        this.jedisFactory = factory;
    }
}