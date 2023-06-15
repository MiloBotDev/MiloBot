package io.github.milobotdev.milobot.api;

import io.github.milobotdev.milobot.api.session.JWTKeys;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Map;

public class API extends ResourceConfig {
    public API() {
        packages("io.github.milobotdev.milobot.api");
        register(App.class);
        setProperties(Map.of("jersey.config.server.wadl.disableWadl", "true"));
        JWTKeys.getInstance(); // Call getInstance() to initialize the JWT keys
    }
}
