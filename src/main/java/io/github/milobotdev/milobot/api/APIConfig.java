package io.github.milobotdev.milobot.api;

import io.github.milobotdev.milobot.api.session.JWTKeysManager;
import org.glassfish.jersey.server.ResourceConfig;

import java.util.Map;

public class APIConfig extends ResourceConfig {
    public APIConfig() {
        packages("io.github.milobotdev.milobot.api");
        register(App.class);
        register(APIHelpers.class);
        register(Login.class);
        setProperties(Map.of("jersey.config.server.wadl.disableWadl", "true"));
        JWTKeysManager.getInstance(); // Call getInstance() to initialize the JWT keys
    }
}
