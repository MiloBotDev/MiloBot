package io.github.milobotdev.milobot.api;

import com.google.gson.Gson;
import io.github.milobotdev.discordoauth2api.DiscordOAuth2API;
import io.github.milobotdev.discordoauth2api.HttpException;
import io.github.milobotdev.discordoauth2api.models.AccessTokenResponse;
import io.github.milobotdev.milobot.api.session.JWTException;
import io.github.milobotdev.milobot.api.session.JWTManager;
import io.github.milobotdev.milobot.utility.Config;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;

@Path("/")
public class Login {

    private Logger logger = LoggerFactory.getLogger(Login.class);

    @GET
    @Path("/do-login")
    @Produces(MediaType.APPLICATION_JSON)
    public LoginReturnData doLogin(@QueryParam("code") String code) {
        //System.out.println("code: " + code);
        Date instantBeforeCodeExchange = new Date();
        AccessTokenResponse resp;
        try {
            resp = DiscordOAuth2API.exchangeAccessToken(Config.getInstance().getBotClientId(),
                    Config.getInstance().getBotSecret(), code, Config.getInstance().apiRedirectUri());

        } catch (IOException e) {
            logger.error("IOException thrown while exchanging access token", e);
            throw new WebApplicationException();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("InterruptedException thrown while exchanging access token", e);
            throw new WebApplicationException();
        } catch (HttpException e) {
            logger.error("DiscordOAuth2API HttpException thrown while exchanging access token", e);
            throw new WebApplicationException();
        }
        // split resp.scope() into a list of strings whitespaces
        // check if the list contains "identify" and "guilds"
        // if not, throw an exception

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
