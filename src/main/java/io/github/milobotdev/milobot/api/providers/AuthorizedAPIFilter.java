package io.github.milobotdev.milobot.api.providers;

import com.google.gson.Gson;
import io.github.milobotdev.milobot.api.models.AccessJwtData;
import io.github.milobotdev.milobot.api.providers.annotations.AuthorizedAPI;
import io.github.milobotdev.milobot.api.session.JWTException;
import io.github.milobotdev.milobot.api.session.JWTManager;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.io.IOException;

@Provider
@AuthorizedAPI
public class AuthorizedAPIFilter implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String authorization = containerRequestContext.getHeaderString("Authorization");
        if (authorization == null) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            return;
        }
        String[] authParts = authorization.split("\\s+");
        if (authParts.length != 2 || !authParts[0].equals("Bearer")) {
            containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
        } else {
            String jwt = authParts[1];
            try {
                String decryptedJWT = JWTManager.decryptJWT(jwt);
                Gson gson = new Gson();
                AccessJwtData data = gson.fromJson(decryptedJWT, AccessJwtData.class);
                containerRequestContext.setProperty("accessJwtData", data);
            } catch (JWTException e) {
                containerRequestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }
}
