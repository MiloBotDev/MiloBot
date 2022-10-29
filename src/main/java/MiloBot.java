import commands.CommandLoader;
import commands.games.blackjack.BlackjackPlayCmd;
import events.OnReadyEvent;
import events.guild.OnGuildJoin;
import events.guild.OnGuildLeave;
import games.BlackjackGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {

    private final static Logger logger = LoggerFactory.getLogger(MiloBot.class);

    public static void main(String[] args) throws LoginException, InterruptedException {
        Config config = Config.getInstance();

        JDA bot = null;
        bot = JDABuilder.createDefault(config.getBotToken(),
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES)
                .setActivity(Activity.watching("Morbius"))
                .addEventListeners(new OnGuildJoin(), new OnGuildLeave(),
                        new OnReadyEvent())
                .build().awaitReady();

        CommandLoader.loadAllCommands(bot);
    }
}