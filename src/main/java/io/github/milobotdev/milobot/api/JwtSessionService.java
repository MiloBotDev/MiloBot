package io.github.milobotdev.milobot.api;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;

public class JwtSessionService {

    @Context
    ContainerRequestContext context;

    public AccessJwtData getAccessJwtData() {
        return (AccessJwtData) context.getProperty("accessJwtData");
    }
}
