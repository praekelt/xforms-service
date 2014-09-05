package org.praekelt.restforms.core.services.jedis;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class JedisFactory {
    
    private static JedisPool jedisPool;
    private static JedisPoolConfig poolConfig;
    private static JedisClient jedisClient;
        
    @NotEmpty
    private String host;

    @Min(1)
    @Max(65535)
    private int port = 6379;
    
    @NotEmpty
    private String password;
    
    @Min(100)
    @Max(5000)
    private int timeout = 100;
    
    @Min(1)
    @Max(50)
    private int poolSize = 5;
    
    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }
    
    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }
    
    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }
    
    @JsonProperty
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    @JsonProperty
    public void setPoolSize(int poolSize) {
    	this.poolSize = poolSize;
    }
        
    public JedisClient build(Environment env) {
        
        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(this.poolSize);
        
        jedisPool = new JedisPool(
            poolConfig,
            this.host,
            this.port,
            this.timeout,
            this.password
        );
        
        jedisClient = new JedisClient(jedisPool);
        
        env.lifecycle().manage(new Managed() {
            @Override
            public void start() {}

            @Override
            public void stop() {
            	jedisPool.destroy();
            }
        });
        env.healthChecks().register("JedisClient", new JedisClient.JedisHealthCheck(jedisClient));
        
        return jedisClient;
    }
}
