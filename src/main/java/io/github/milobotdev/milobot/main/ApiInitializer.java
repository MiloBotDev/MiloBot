package io.github.milobotdev.milobot.main;

import io.github.milobotdev.milobot.api.API;
import io.github.milobotdev.milobot.api.App;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;


public class ApiInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApiInitializer.class);

    public static void initialize() {
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:8080/"), new API());
        try {
            server.start();
        } catch (IOException e) {
            logger.error("Exception thrown while starting the API server", e);
        }
    }
}
