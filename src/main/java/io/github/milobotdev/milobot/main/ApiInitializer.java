package io.github.milobotdev.milobot.main;

import io.github.milobotdev.milobot.api.APIConfig;
import io.github.milobotdev.milobot.utility.Config;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;


public class ApiInitializer {
    private static final Logger logger = LoggerFactory.getLogger(ApiInitializer.class);

    /**
     * Starts the API server (for the dashboard).
     */
    public static void initialize() {
        int port = Config.getInstance().getApiPort();
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(URI.create("http://localhost:" + port + "/"), new APIConfig());
        try {
            server.start();
        } catch (IOException e) {
            logger.error("Exception thrown while starting the API server", e);
        }
    }
}
