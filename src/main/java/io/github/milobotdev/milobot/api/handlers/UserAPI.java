package io.github.milobotdev.milobot.api.handlers;

import io.github.milobotdev.discordoauth2api.DiscordOAuth2API;
import io.github.milobotdev.discordoauth2api.exceptions.HttpException;
import io.github.milobotdev.discordoauth2api.exceptions.RateLimitExceededException;
import io.github.milobotdev.discordoauth2api.models.Guild;
import io.github.milobotdev.milobot.api.providers.annotations.AuthorizedAPI;
import io.github.milobotdev.milobot.api.services.JwtSessionService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Path("/")
@AuthorizedAPI
public class UserAPI {

    private final Logger logger = LoggerFactory.getLogger(UserAPI.class);

    @Inject
    private JwtSessionService jwtSessionService;

    public record GuildReturnData(String name, String id, String iconUrl) {
    }

    @GET
    @Path("/guilds")
    @Produces(MediaType.APPLICATION_JSON)
    public GuildReturnData[] getGuilds() {
        Guild[] guilds = new Guild[0];
        boolean success = false;
        while (!success) {
            try {
                guilds = DiscordOAuth2API.fetchGuilds(jwtSessionService.getAccessJwtData().accessToken());
                success = true;
            } catch (IOException e) {
                logger.error("IOException thrown while fetching guilds", e);
                throw new WebApplicationException();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("InterruptedException thrown while fetching guilds", e);
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
                    logger.error("DiscordOAuth2API HttpException thrown while fetching guilds", e);
                    System.out.println(e.getResponse().body());
                    throw new WebApplicationException();
                }
            }
        }

        GuildReturnData[] guildReturnData = new GuildReturnData[guilds.length];
        for (int i = 0; i < guilds.length; i++) {
            String iconUrl = null;
            if (guilds[i].icon() != null) {
                iconUrl = "https://cdn.discordapp.com/icons/" + guilds[i].id() + "/" + guilds[i].icon() + ".png";
            }
            guildReturnData[i] = new GuildReturnData(guilds[i].name(), guilds[i].id(), iconUrl);
        }

        return guildReturnData;
    }
}
