package io.github.milobotdev.milobot.api.handlers;

import com.google.gson.Gson;
import io.github.milobotdev.discordoauth2api.DiscordOAuth2API;
import io.github.milobotdev.discordoauth2api.exceptions.HttpException;
import io.github.milobotdev.discordoauth2api.exceptions.RateLimitExceededException;
import io.github.milobotdev.discordoauth2api.models.AccessTokenResponse;
import io.github.milobotdev.milobot.api.models.AccessJwtData;
import io.github.milobotdev.milobot.api.models.LoginReturnData;
import io.github.milobotdev.milobot.api.session.JWTException;
import io.github.milobotdev.milobot.api.session.JWTManager;
import io.github.milobotdev.milobot.utility.Config;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Path("/")
public class Login {

    private final Logger logger = LoggerFactory.getLogger(Login.class);

    @GET
    @Path("/do-login")
    @Produces(MediaType.APPLICATION_JSON)
    public LoginReturnData doLogin(@QueryParam("code") String code) {
        Date instantBeforeCodeExchange = new Date();
        AccessTokenResponse resp = null;
        boolean success = false;
        while (!success) {
            try {
                resp = DiscordOAuth2API.exchangeAccessToken(Config.getInstance().getBotClientId(),
                        Config.getInstance().getBotSecret(), code, Config.getInstance().apiRedirectUri());
                success = true;
            } catch (IOException e) {
                logger.error("IOException thrown while exchanging access token", e);
                throw new WebApplicationException();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("InterruptedException thrown while exchanging access token", e);
                throw new WebApplicationException();
            } catch (RateLimitExceededException e) {
                try {
                    Thread.sleep((long) Math.ceil(e.getRateLimitExceededResponse().retryAfter() * 1000));
                } catch (InterruptedException interruptedException) {
                    Thread.currentThread().interrupt();
                    logger.error("InterruptedException thrown while sleeping to retry after rate limit exceeded", interruptedException);
                    throw new WebApplicationException();
                }
            } catch (HttpException e) {
                if (e.getResponse().statusCode() == 400) {
                    throw new WebApplicationException(400);
                } else {
                    logger.error("DiscordOAuth2API HttpException thrown while exchanging access token", e);
                    throw new WebApplicationException();
                }
            }
        }

        List<String> scopes = List.of(resp.scope().split(" "));
        if (!scopes.contains("identify") || !scopes.contains("guilds")) {
            logger.trace("User did not authorize identify and guilds scopes");
            throw new WebApplicationException();
        }

        Date accessTokenExpiry = Date.from(instantBeforeCodeExchange.toInstant().plusSeconds(resp.expiresIn()));
        AccessJwtData data = new AccessJwtData(resp.accessToken(), accessTokenExpiry, resp.refreshToken());
        String accessJwt;
        try {
            accessJwt = JWTManager.generateJWT(new Gson().toJson(data, AccessJwtData.class));
        } catch (JWTException e) {
            logger.error("JWTException thrown while generating access JWT", e);
            throw new WebApplicationException();
        }
        return new LoginReturnData(accessJwt);
    }
}
