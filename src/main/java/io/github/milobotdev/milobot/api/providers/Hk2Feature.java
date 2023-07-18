package io.github.milobotdev.milobot.api.providers;

import io.github.milobotdev.milobot.api.services.JwtSessionService;
import jakarta.ws.rs.core.Feature;

import jakarta.ws.rs.core.FeatureContext;

import jakarta.ws.rs.ext.Provider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

@Provider
public class Hk2Feature implements Feature {

    public boolean configure(FeatureContext context) {
        context.register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(JwtSessionService.class).to(JwtSessionService.class);
            }
        });

        return true;
    }
}
