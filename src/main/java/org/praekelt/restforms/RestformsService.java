package org.praekelt.restforms;

import org.praekelt.restforms.resources.FormPlayer;
import org.praekelt.restforms.resources.Forms;
import org.praekelt.restforms.resources.IndexResource;
import org.praekelt.tools.JedisClient;

import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
 
public class RestformsService extends Application<RestformsConfiguration> {
 
    public static void main(String[] args) throws Exception {
        new RestformsService().run(args);
    }
 
	@Override
	public void initialize(Bootstrap<RestformsConfiguration> bootstrap) {
	}

	@Override
	public void run(RestformsConfiguration configuration, Environment environment)
			throws Exception {
		final IndexResource resource = new IndexResource(
		        configuration.getTemplate(),
		        configuration.getDefaultName()
		    );
			JedisClient jedisClient = configuration.getJedisFactory().build(environment);
		    environment.jersey().register(resource);
		    environment.jersey().register(new Forms(jedisClient));
		    environment.jersey().register(new FormPlayer(jedisClient));
	}
    
}