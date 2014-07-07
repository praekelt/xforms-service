package org.praekelt.service;

import org.praekelt.Forms;
import org.praekelt.resources.IndexResource;
import org.praekelt.tools.JedisClient;
import org.praekelt.tools.JedisFactory;
import org.praekelt.vumi.FormPlayer;

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