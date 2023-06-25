package io.github.milobotdev.milobot.api;

import com.google.gson.Gson;
import io.github.milobotdev.discordoauth2api.DiscordOAuth2API;
import io.github.milobotdev.discordoauth2api.HttpException;
import io.github.milobotdev.discordoauth2api.models.AccessTokenResponse;
import io.github.milobotdev.milobot.utility.Config;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Path("/")
public class Login {

    private Logger logger = LoggerFactory.getLogger(Login.class);

    @GET
    @Path("/do-login")
    @Produces(MediaType.APPLICATION_JSON)
    public LoginReturnData doLogin(@QueryParam("code") String code) {
        System.out.println("code: " + code);
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
            logger.error("DiscordOAuth2APIException thrown while exchanging access token", e);
            throw new WebApplicationException();
        }
        return new LoginReturnData(new Gson().toJson(resp, AccessTokenResponse.class));
    }
}
