package org.praekelt.restforms;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.praekelt.restforms.resources.FormPlayer;
import org.praekelt.restforms.resources.Forms;
import org.praekelt.restforms.resources.IndexResource;
import org.praekelt.restforms.resources.UploadFileService;
import org.praekelt.restforms.core.JedisClient;

public class RestformsService extends Application<RestformsConfiguration> {

    public static void main(String[] args) throws Exception {
        new RestformsService().run(args);
    }

    @Override
    public void initialize(Bootstrap<RestformsConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle("/assets/", "/assets"));
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
        environment.jersey().register(new UploadFileService(jedisClient));
        environment.jersey().register(new FormPlayer(jedisClient));
    }

}
