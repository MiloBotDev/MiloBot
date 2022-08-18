import commands.CommandHandler;
import commands.CommandLoader;
import commands.games.blackjack.BlackjackPlayCmd;
import events.OnButtonClick;
import events.OnReadyEvent;
import events.guild.OnGuildJoinEvent;
import events.guild.OnGuildLeaveEvent;
import games.Blackjack;
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
import utility.Lobby;
import utility.Paginator;

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

        JDA bot = JDABuilder.createDefault(config.getBotToken(),
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES)
                .setActivity(Activity.watching("Morbius"))
                .addEventListeners(new CommandHandler(), new OnGuildJoinEvent(), new OnGuildLeaveEvent(),
                        new OnReadyEvent(), new OnButtonClick())
                .build().awaitReady();

        CommandLoader.loadAllCommands(bot);

        Timer timer = new Timer();
        TimerTask clearBlackjackInstances = clearInstances(bot);
        timer.schedule(clearBlackjackInstances, 1000 * 60 * 60, 1000 * 60 * 60);
    }

    /**
     * Clears instances that haven't been used for over 15 minutes every hour.
     */
    @NotNull
    private static TimerTask clearInstances(JDA bot) {
        return new TimerTask() {
            @Override
            public void run() {
                logger.info("Attempting to clear idle instances.");

                DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
                TextChannel logs = Objects.requireNonNull(bot.getGuildById(Config.getInstance().getTestGuildId()))
                        .getTextChannelsByName(Config.getInstance().getLoggingChannelName(), true).get(0);
                long currentNanoTime = System.nanoTime();

                Map<String, Blackjack> blackjackGames = BlackjackPlayCmd.blackjackGames;
                List<String> blackjackInstancesToRemove = new ArrayList<>();
                blackjackGames.forEach(
                        (s, blackjack) -> {
                            long startTime = blackjack.getStartTime();
                            long elapsedTime = currentNanoTime - startTime;
                            long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
                            if (elapsedTimeSeconds > 900) {
                                logger.info(String.format("Blackjack instance by: %s timed out. Time elapsed %d seconds.",
                                        s, elapsedTimeSeconds));
                                blackjackInstancesToRemove.add(s);
                            }
                        }
                );

                Map<String, Paginator> paginatorInstances = Paginator.paginatorInstances;
                List<String> paginatorInstancesToRemove = new ArrayList<>();
                paginatorInstances.forEach(
                        (s, paginator) -> {
                            long startTime = paginator.getStartTime();
                            long elapsedTime = currentNanoTime - startTime;
                            long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
                            if (elapsedTimeSeconds > 900) {
                                logger.info(String.format("Paginator instance by: %s timed out. Time elapsed %d seconds.",
                                        s, elapsedTimeSeconds));
                                paginatorInstancesToRemove.add(s);
                            }
                        }
                );

                Map<String, Lobby> lobbyInstances = Lobby.lobbyInstances;
                List<String> lobbyInstancesToRemove = new ArrayList<>();
                lobbyInstances.forEach(
                        (s, lobby) -> {
                            long startTime = lobby.getStartTime();
                            long elapsedTime = currentNanoTime - startTime;
                            long elapsedTimeSeconds = TimeUnit.SECONDS.convert(elapsedTime, TimeUnit.NANOSECONDS);
                            if (elapsedTimeSeconds > 900) {
                                logger.info(String.format("Lobby instance by: %s timed out. Time elapsed %d seconds.",
                                        s, elapsedTimeSeconds));
                                lobbyInstancesToRemove.add(s);
                            }
                        }
                );

                if (blackjackInstancesToRemove.size() == 0) {
                    logger.info("No blackjack instances timed out.");
                } else {
                    for (String s : blackjackInstancesToRemove) {
                        blackjackGames.remove(s);
                    }
                    logger.info(String.format("Removed %d blackjack instances.", blackjackInstancesToRemove.size()));
                }

                if (paginatorInstancesToRemove.size() == 0) {
                    logger.info("No paginator instances timed out.");
                } else {
                    for (String s : paginatorInstancesToRemove) {
                        paginatorInstances.remove(s);
                    }
                    logger.info(String.format("Removed %d paginator instances.", paginatorInstancesToRemove.size()));
                }

                if (lobbyInstancesToRemove.size() == 0) {
                    logger.info("No lobby instances timed out.");
                } else {
                    for (String s : lobbyInstancesToRemove) {
                        lobbyInstances.remove(s);
                    }
                    logger.info(String.format("Removed %d lobby instances.", lobbyInstancesToRemove.size()));
                }

                EmbedBuilder logEmbed = new EmbedBuilder();
                logEmbed.setTitle("Idle Instance Cleanup");
                logEmbed.setColor(Color.green);
                logEmbed.setFooter(dtf.format(LocalDateTime.now()));
                logEmbed.setDescription(String.format("Cleared `%d` blackjack instances.\nCleared `%d` paginator instances.",
                        blackjackInstancesToRemove.size(), paginatorInstancesToRemove.size()));
                logs.sendMessageEmbeds(logEmbed.build()).queue();
            }
        };
    }
}
