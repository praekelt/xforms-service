package org.praekelt.restforms.core;

import javax.validation.constraints.NotNull;
import org.praekelt.restforms.core.services.jedis.JedisFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
 
public class RestformsConfiguration extends Configuration {
    
    private final JedisFactory jedisFactory = new JedisFactory();
    
    @NotNull
    private String template;

    @NotNull
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
    
    @JsonProperty("jedisFactory")
    public JedisFactory getJedisFactory() {
        return jedisFactory;
    }
}