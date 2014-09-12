package org.praekelt.restforms.core.services.jedis;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public final class JedisFactory {
    
    private static JedisPool jedisPool;
    private static JedisPoolConfig poolConfig;
    private static JedisClient jedisClient;
    
    public static JedisPool getJedisPool() {
        return jedisPool;
    }
        
    @NotEmpty
    private String host;

    @Min(1)
    @Max(65535)
    private int port = 6379;
    
    @NotEmpty
    private String password;
    
    private int expires;
    
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
    public void setExpires(int expires) {
        this.expires = expires;
    }
    
    @JsonProperty
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    @JsonProperty
    public void setPoolSize(int poolSize) {
    	this.poolSize = poolSize;
    }
    
    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }
    
    @JsonProperty
    public int getExpires() {
        return expires;
    }

    @JsonProperty
    public int getTimeout() {
        return timeout;
    }
    
    @JsonProperty
    public int getPoolSize() {
        return poolSize;
    }
        
    public JedisClient build() {
        
        poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(this.getPoolSize());
        
        jedisPool = new JedisPool(
            poolConfig,
            this.getHost(),
            this.getPort(),
            this.getTimeout(),
            this.getPassword()
        );
        jedisClient = new JedisClient(jedisPool, expires);
        
        return jedisClient;
    }
}
