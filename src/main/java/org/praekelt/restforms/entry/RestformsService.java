package org.praekelt.restforms.entry;

import org.praekelt.restforms.core.RestformsConfiguration;
import org.praekelt.restforms.core.services.JedisClient;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.praekelt.restforms.core.resources.Forms;
 
public class RestformsService extends Application<RestformsConfiguration> {
    
    private static JedisClient jc;
    
    public static void main(String[] args) throws Exception {
        new RestformsService().run(args);
    }
 
    @Override
    public void initialize(Bootstrap<RestformsConfiguration> bootstrap) {}

    @Override
    public void run(RestformsConfiguration configuration, Environment environment) {
        
        /*
            the forms resource is being registered simply
            to allow the application to successfully
            bootstrap. it will soon be replaced with a
            new forms implementation.
        */
        
        jc = configuration.getJedisFactory().build(environment);
        environment.jersey().register(new Forms(jc));
    }
    
}