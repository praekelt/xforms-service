package org.praekelt.restforms.entry;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.resources.FormsResource;
 
public class RestformsService extends Application<RestformsConfiguration> {
    
    public static void main(String[] args) throws Exception {
        new RestformsService().run(args);
    }
 
    @Override
    public void initialize(Bootstrap<RestformsConfiguration> bootstrap) {}

    @Override
    public void run(RestformsConfiguration cfg, Environment env) {
        
        (env.jersey()).register(new FormsResource(cfg, env));
    }
    
}