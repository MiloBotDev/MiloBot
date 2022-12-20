package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.WordleDao;
import io.github.milobotdev.milobot.database.model.Wordle;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.Users;
import io.github.milobotdev.milobot.utility.chart.BarChart;
import io.github.milobotdev.milobot.utility.paginator.PaginatorWithImages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * View all the leaderboards for the Wordle command.
 */
public class WordleLeaderboardCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes, EventListeners {

    private final ExecutorService executorService;
    private static final UserDao userDao = UserDao.getInstance();
    private static final WordleDao wordleDao = WordleDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(WordleLeaderboardCmd.class);
    private static final Users userUtil = Users.getInstance();

    public WordleLeaderboardCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        SelectionMenu menu = SelectionMenu.create(event.getAuthor().getId() + ":wordleLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Fastest Time", "fastestTime")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Games Played", "totalGames")
                .build();
        event.getChannel().sendMessage("Wordle Leaderboard Selection").setActionRow(menu).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        SelectionMenu menu = SelectionMenu.create(event.getUser().getId() + ":wordleLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Fastest Time", "fastestTime")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Games Played", "totalGames")
                .addOption("Current Streak", "currentStreak")
                .build();
        event.reply("Wordle Leaderboard Selection").addActionRow(menu).queue();
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("leaderboard", "View the wordle leaderboards.");
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull List<EventListener> getEventListeners() {
        return List.of(listener);
    }

    private final ListenerAdapter listener = new ListenerAdapter() {
        @Override
        public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
            String[] id = event.getComponentId().split(":");
            String authorId = id[0];
            String type = id[1];
            User user = event.getUser();

            if (user.getId().equals(authorId) && type.equals("wordleLeaderboard")) {
                event.deferEdit().queue();
                String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                try {
                    switch (option) {
                        case "highestStreak" -> {
                            List<Wordle> topHighestStreak = wordleDao.getTopHighestStreak();
                            buildWordleEmbeds(event, topHighestStreak, "Highest Streak", event.getJDA());
                        }
                        case "fastestTime" -> {
                            List<Wordle> topFastestTime = wordleDao.getTopFastestTime();
                            buildWordleEmbeds(event, topFastestTime, "Fastest Time", event.getJDA());
                        }
                        case "totalWins" -> {
                            List<Wordle> topTotalWins = wordleDao.getTopTotalWins();
                            buildWordleEmbeds(event, topTotalWins, "Total Wins", event.getJDA());
                        }
                        case "totalGames" -> {
                            List<Wordle> topTotalGames = wordleDao.getTopTotalGames();
                            buildWordleEmbeds(event, topTotalGames, "Total Games Played", event.getJDA());
                        }
                        case "currentStreak" -> {
                            List<Wordle> topCurrentStreak = wordleDao.getTopCurrentStreak();
                            buildWordleEmbeds(event, topCurrentStreak, "Current Streak", event.getJDA());
                        }
                    }
                } catch (SQLException | IOException e) {
                    logger.error("Failed to get the wordle leaderboard", e);
                }
            }
        }
    };

    public static void buildWordleEmbeds(@NotNull SelectionMenuEvent event,
                                         @NotNull List<Wordle> wordles, String title,
                                         JDA jda) throws IOException {
        List<MessageEmbed> embeds = new ArrayList<>();
        List<EmbedBuilder> builders = new ArrayList<>();
        List<byte[]> charts = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        final BarChart[] chart = {new BarChart("Wordle Leaderboard", "User", title, title)};
        final Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED,
                Color.YELLOW, Color.LIGHT_GRAY, Color.decode("#90EE90")};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] rank = {1};
        final int[] counter = {0};
        final int[] chartCounter = {0};
        wordles.forEach((wordle) -> {
            try (Connection con = DatabaseConnection.getConnection()) {
                long discordId = Objects.requireNonNull(userDao.getUserById(con, wordle.getUserId(), RowLockType.NONE)).getDiscordId();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Highest Streak" -> {
                        desc[0].append(String.format("`%d.` %s - %d games.\n", rank[0], name, wordle.getHighestStreak()));
                        chart[0].addBar(name, wordle.getHighestStreak(), colors[counter[0]]);
                    }
                    case "Fastest Time" -> {
                        desc[0].append(String.format("`%d.` %s - %d seconds.\n", rank[0], name, wordle.getFastestTime()));
                        chart[0].addBar(name, wordle.getFastestTime(), colors[counter[0]]);
                    }
                    case "Total Wins" -> {
                        desc[0].append(String.format("`%d.` %s - %d wins.\n", rank[0], name, wordle.getWins()));
                        chart[0].addBar(name, wordle.getWins(), colors[counter[0]]);
                    }
                    case "Total Games Played" -> {
                        desc[0].append(String.format("`%d.` %s - %d games.\n", rank[0], name, wordle.getGamesPlayed()));
                        chart[0].addBar(name, wordle.getGamesPlayed(), colors[counter[0]]);
                    }
                    case "Current Streak" -> {
                        desc[0].append(String.format("`%d.` %s - %d games.\n", rank[0], name, wordle.getCurrentStreak()));
                        chart[0].addBar(name, wordle.getCurrentStreak(), colors[counter[0]]);
                    }
                }
                rank[0]++;
                counter[0]++;
                if (rank[0] % 2 == 0) {
                    counter[0] = 0;
                    embed[0].setDescription(desc[0]);
                    System.out.println("att: " + "attachment://chart" + chartCounter[0] + ".png");
                    //embed[0].setImage("attachment://chart" + chartCounter[0] + ".png");
                    chartCounter[0]++;
                    charts.add(chart[0].createBarChart());
                    embeds.add(embed[0].build());
                    builders.add(embed[0]);
                    embed[0] = new EmbedBuilder();
                    desc[0] = new StringBuilder();
                    chart[0] = new BarChart("Wordle Leaderboard", title, "User", title);
                    embed[0].setTitle(title);
                    embed[0].setColor(Color.BLUE);
                }
            } catch (SQLException e) {
                logger.error("Failed to get the wordle leaderboard", e);
            }

        });

        embed[0].setImage("attachment://chart0.png");

        //embed[0].setDescription(desc[0]);
        /*System.out.println("att: " + "attachment://chart" + chartCounter[0] + ".png");
        embed[0].setImage("attachment://chart" + rank[0] + ".png");
        charts.add(chart[0].createBarChart());
        embeds.add(embed[0].build());*/

        AtomicInteger chartNumCounter = new AtomicInteger();
        PaginatorWithImages paginator = new PaginatorWithImages(event.getUser(), embeds, (i, m) -> {
            int num = chartNumCounter.getAndIncrement();
            System.out.println("num: " + num + " i: " + i);
            return m.editMessageEmbeds(new EmbedBuilder(builders.get(i)).setImage("attachment://chart" + num + ".png").build()).addFile(charts.get(i), "chart" + num + ".png");
        });

        /*Paginator paginator = new Paginator(event.getUser(), embeds);
        WebhookMessageAction<Message> action = event.getHook().sendMessageEmbeds(paginator.currentPage())
                .addActionRows(paginator.getActionRows());

        for (int i=0; i<charts.size(); i++) {
            action = action.addFile(charts.get(i), "chart" + i + ".png");
        }*/

        event.getHook().sendMessageEmbeds(paginator.currentPage()).addFile(charts.get(0), "chart0.png")
                .addActionRows(paginator.getActionRows()).queue(paginator::initialize);
    }



}

