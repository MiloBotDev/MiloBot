package io.github.milobotdev.milobot.main;

import io.github.milobotdev.milobot.api.App;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;


public class ApiInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApiInitializer.class);

    public static void initialize() {
        ResourceConfig config = new ResourceConfig();
        config.register(App.class);
        config.setProperties(Map.of("jersey.config.server.wadl.disableWadl", "true"));
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/"), config);
        try {
            server.start();
        } catch (IOException e) {
            logger.error("Exception while starting the API server", e);
        }
    }
}
