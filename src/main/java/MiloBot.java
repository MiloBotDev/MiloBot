import commands.NewCommandLoader;
import commands.games.blackjack.BlackjackPlayCmd;
import events.OnButtonClick;
import events.OnReadyEvent;
import events.OnSelectionMenu;
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

        // There are two command handler versions: the old one called CommandHandler
        // and the new one called NewCommandHandler. The NewCommandHandler is still under
        // development, so for legacy commands use the old one.
        // To select one, comment out the error message and System.exit(1) and uncomment
        // the specific command handler loader you want to use
        // also, when committing, remember that you DO NOT commit this file!!!
        // This is just temporarily like this until the new command handler is finished

        System.out.println("ERROR: Command handler version not selected. Please open MiloBot.java file and follow instructions.");
        System.exit(1);

        JDA bot = null;

        // This is the old command handler
        /*
        bot = JDABuilder.createDefault(config.getBotToken(),
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES)
                .setActivity(Activity.watching("Morbius"))
                .addEventListeners(new CommandHandler, new OnGuildJoin(), new OnGuildLeave(),
                        new OnReadyEvent(), new OnButtonClick(), new OnSelectionMenu())
                .build().awaitReady();
        CommandLoader.loadAllCommands(bot);
         */
        // uncomment the below to generate updated documentation
//        CommandLoader.generateCommandDocumentation();

        // This is the new command handler:


        bot = JDABuilder.createDefault(config.getBotToken(),
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES)
                .setActivity(Activity.watching("Morbius"))
                .addEventListeners(new OnGuildJoin(), new OnGuildLeave(),
                        new OnReadyEvent(), new OnButtonClick(), new OnSelectionMenu())
                .build().awaitReady();

        NewCommandLoader.loadAllCommands(bot);

        Timer timer = new Timer();
        TimerTask clearBlackjackInstances = clearInstances(bot);
        timer.schedule(clearBlackjackInstances, 1000 * 60 * 60, 1000 * 60 * 60);
    }

    /**
     * Clears instances that haven't been used for over 15 minutes every hour.
     */
    @Deprecated(since="9/8/22, Everything will have its own idle instance cleanup", forRemoval=true)
    @NotNull
    private static TimerTask clearInstances(JDA bot) {
        return new TimerTask() {
            @Override
            public void run() {
                logger.debug("Attempting to clear idle instances.");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                TextChannel logs = Objects.requireNonNull(bot.getGuildById(Config.getInstance().getTestGuildId()))
                        .getTextChannelsByName(Config.getInstance().getLoggingChannelName(), true).get(0);
                long currentNanoTime = System.nanoTime();

                Map<Long, BlackjackGame> blackjackGames = BlackjackPlayCmd.blackjackGames;
                List<Long> blackjackInstancesToRemove = new ArrayList<>();
                blackjackGames.forEach(
                        (s, blackjackGame) -> {
                            long startTime = blackjackGame.getStartTime();
                            long elapsedTime = currentNanoTime - startTime;
                            long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
                            if (elapsedTimeSeconds > 900) {
                                logger.trace(String.format("Blackjack instance by: %s timed out. Time elapsed %d seconds.",
                                        s, elapsedTimeSeconds));
                                blackjackInstancesToRemove.add(s);
                            }
                        }
                );

                if (blackjackInstancesToRemove.size() == 0) {
                    logger.trace("No blackjack instances timed out.");
                } else {
                    for (Long s : blackjackInstancesToRemove) {
                        blackjackGames.remove(s);
                    }
                    logger.trace(String.format("Removed %d blackjack instances.", blackjackInstancesToRemove.size()));
                }

                EmbedBuilder logEmbed = new EmbedBuilder();
                logEmbed.setTitle("Idle Instance Cleanup");
                logEmbed.setColor(Color.green);
                logEmbed.setFooter(dtf.format(LocalDateTime.now()));
                logEmbed.setDescription(String.format("Cleared `%d` blackjack instances.",
                        blackjackInstancesToRemove.size()));
                logs.sendMessageEmbeds(logEmbed.build()).queue();
            }
        };
    }
}