package io.github.milobotdev.milobot.api.services;

import io.github.milobotdev.milobot.api.models.AccessJwtData;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

public class JwtSessionService {

    @Context
    private ContainerRequestContext context;

    public AccessJwtData getAccessJwtData() {
        return (AccessJwtData) context.getProperty("accessJwtData");
    }
}
