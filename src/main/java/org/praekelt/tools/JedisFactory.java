package org.praekelt.tools;

import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

import org.hibernate.validator.constraints.NotEmpty;

import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.fasterxml.jackson.annotation.JsonProperty;

public class JedisFactory {

    @NotEmpty
    private String host = "127.0.0.1";

    @Min(1)
    @Max(65535)
    private int port = 6379;

    @NotEmpty
    private String password = "";

    @Min(100)
    @Max(5000)
    private int timeout = 100;

    @Min(1)
    @Max(50)
    private int poolsize = 50;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String host) {
        this.host = host;
    }

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getPassword() {
        return password;
    }

    @JsonProperty
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty
    public int getTimeout() {
        return timeout;
    }

    @JsonProperty
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @JsonProperty
    public int getPoolsize() {
        return poolsize;
    }

    @JsonProperty
    public void setPoolsize(int poolsize) {
        this.poolsize = poolsize;
    }

    public JedisClient build(Environment environment) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(getPoolsize());
        final JedisPool pool = new JedisPool(
                poolConfig,
                getHost(),
                getPort(),
                getTimeout());

        environment.lifecycle().manage(new Managed() {
            @Override
            public void start() {
            }

            @Override
            public void stop() {
                pool.destroy();
            }
        });
        return new JedisClient(pool);
    }
}
