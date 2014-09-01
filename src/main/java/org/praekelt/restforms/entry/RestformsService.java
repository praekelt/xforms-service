package org.praekelt.restforms.entry;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.resources.AnswersResource;
import org.praekelt.restforms.core.resources.FormsResource;
import org.praekelt.restforms.core.services.JedisClient;
 
public class RestformsService extends Application<RestformsConfiguration> {
    
    public static void main(String[] args) throws Exception {
        new RestformsService().run(args);
    }
 
    @Override
    public void initialize(Bootstrap<RestformsConfiguration> bootstrap) {}

    @Override
    public void run(RestformsConfiguration cfg, Environment env) {
        
        final JedisClient jedisClient = cfg.getJedisFactory().build(env);
        
        (env.jersey()).register(new FormsResource(jedisClient));
        (env.jersey()).register(new AnswersResource(jedisClient));
    }
    
}