package io.github.milobotdev.milobot.commands.games.uno;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.database.dao.UnoDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.Uno;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.Users;
import io.github.milobotdev.milobot.utility.chart.BarChart;
import io.github.milobotdev.milobot.utility.paginator.PaginatorWithImages;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * View all the leaderboards for the Wordle command.
 */
public class UnoLeaderboardCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes, EventListeners {

    private static final int NUM_USERS_ON_LEADERBOARD = 10;
    private final ExecutorService executorService;
    private static final UserDao userDao = UserDao.getInstance();
    private static final UnoDao unoDao = UnoDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(UnoLeaderboardCmd.class);
    private static final Users userUtil = Users.getInstance();

    public UnoLeaderboardCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        StringSelectMenu menu = StringSelectMenu.create(event.getAuthor().getId() + ":unoLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Games Played", "totalGames")
                .addOption("Total Cards Played", "totalCardsPlayed")
                .addOption("Total Cards Drawn", "totalCardsDrawn")
                .build();
        event.getChannel().sendMessage("Uno Leaderboard Selection").setActionRow(menu).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        StringSelectMenu menu = StringSelectMenu.create(event.getUser().getId() + ":unoLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Games Played", "totalGames")
                .addOption("Total Cards Played", "totalCardsPlayed")
                .addOption("Total Cards Drawn", "totalCardsDrawn")
                .build();
        event.reply("Uno Leaderboard Selection").addActionRow(menu).queue();
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(new SubcommandData("leaderboard", "View the uno leaderboards."));
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
        public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
            String[] id = event.getComponentId().split(":");
            String authorId = id[0];
            String type = id[1];
            User user = event.getUser();

            if (user.getId().equals(authorId) && type.equals("unoLeaderboard")) {
                event.deferEdit().queue();
                String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                try {
                    switch (option) {
                        case "highestStreak" -> {
                            List<Uno> topHighestStreak = unoDao.getUnosLeaderboard(UnoDao.UnoLeaderboardType.HIGHEST_STREAK);
                            buildUnoEmbeds(event, topHighestStreak, "Highest Streak", event.getJDA());
                        }
                        case "currentStreak" -> {
                            List<Uno> topCurrentStreak = unoDao.getUnosLeaderboard(UnoDao.UnoLeaderboardType.CURRENT_STREAK);
                            buildUnoEmbeds(event, topCurrentStreak, "Current Streak", event.getJDA());
                        }
                        case "totalGames" -> {
                            List<Uno> topTotalGamesPlayed = unoDao.getUnosLeaderboard(UnoDao.UnoLeaderboardType.TOTAL_GAMES_PLAYED);
                            buildUnoEmbeds(event, topTotalGamesPlayed, "Total Games Played", event.getJDA());
                        }
                        case "totalWins" -> {
                            List<Uno> topTotalWins = unoDao.getUnosLeaderboard(UnoDao.UnoLeaderboardType.TOTAL_WINS);
                            buildUnoEmbeds(event, topTotalWins, "Total Wins", event.getJDA());
                        }
                        case "totalCardsPlayed" -> {
                            List<Uno> topTotalCardsPlayed = unoDao.getUnosLeaderboard(UnoDao.UnoLeaderboardType.TOTAL_CARDS_PLAYED);
                            buildUnoEmbeds(event, topTotalCardsPlayed, "Total Cards Played", event.getJDA());
                        }
                        case "totalCardsDrawn" -> {
                            List<Uno> topTotalCardsDrawn = unoDao.getUnosLeaderboard(UnoDao.UnoLeaderboardType.TOTAL_CARDS_DRAWN);
                            buildUnoEmbeds(event, topTotalCardsDrawn, "Total Cards Drawn", event.getJDA());
                        }
                    }
                } catch (SQLException | IOException e) {
                    logger.error("Failed to get the uno leaderboard", e);
                }
            }
        }
    };

    public static void buildUnoEmbeds(@NotNull StringSelectInteractionEvent event, @NotNull List<Uno> unos, String title, JDA jda)
            throws IOException {
        List<MessageEmbed> embeds = new ArrayList<>();
        List<byte[]> charts = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        final BarChart[] chart = {new BarChart("Uno Leaderboard", "User", title, title)};
        final Color[] colors = {Color.BLUE, Color.CYAN, Color.GREEN, Color.MAGENTA, Color.ORANGE, Color.PINK, Color.RED,
                Color.YELLOW, Color.LIGHT_GRAY, Color.decode("#90EE90")};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] rank = {1};
        final int[] counter = {0};
        final int[] chartCounter = {0};
        AtomicBoolean ran = new AtomicBoolean(false);
        unos.forEach((uno) -> {
            ran.set(true);
            try (Connection con = DatabaseConnection.getConnection()) {
                long discordId = Objects.requireNonNull(userDao.getUserById(con, uno.getUserId(), RowLockType.NONE)).getDiscordId();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Highest Streak" -> {
                        desc[0].append(String.format("`%d.` %s - %d games.\n", rank[0], name, uno.getHighestStreak()));
                        chart[0].addBar(name, uno.getHighestStreak(), colors[counter[0]]);
                    }
                    case "Current Streak" -> {
                        desc[0].append(String.format("`%d.` %s - %d games.\n", rank[0], name, uno.getStreak()));
                        chart[0].addBar(name, uno.getStreak(), colors[counter[0]]);
                    }
                    case "Total Games Played" -> {
                        desc[0].append(String.format("`%d.` %s - %d games.\n", rank[0], name, uno.getStreak()));
                        chart[0].addBar(name, uno.getTotalCardsPlayed(), colors[counter[0]]);
                    }
                    case "Total Wins" -> {
                        desc[0].append(String.format("`%d.` %s - %d wins.\n", rank[0], name, uno.getTotalWins()));
                        chart[0].addBar(name, uno.getTotalWins(), colors[counter[0]]);
                    }
                    case "Total Cards Played" -> {
                        desc[0].append(String.format("`%d.` %s - %d cards.\n", rank[0], name, uno.getTotalCardsPlayed()));
                        chart[0].addBar(name, uno.getTotalCardsPlayed(), colors[counter[0]]);
                    }
                    case "Total Cards Drawn" -> {
                        desc[0].append(String.format("`%d.` %s - %d cards.\n", rank[0], name, uno.getTotalCardsDrawn()));
                        chart[0].addBar(name, uno.getTotalCardsDrawn(), colors[counter[0]]);
                    }
                }
                rank[0]++;
                counter[0]++;
                if ((rank[0]-1) % NUM_USERS_ON_LEADERBOARD == 0) {
                    counter[0] = 0;
                    embed[0].setDescription(desc[0]);
                    embed[0].setImage("attachment://chart" + chartCounter[0] + ".png");
                    chartCounter[0]++;
                    charts.add(chart[0].createBarChart());
                    embeds.add(embed[0].build());
                    embed[0] = new EmbedBuilder();
                    desc[0] = new StringBuilder();
                    chart[0] = new BarChart("Uno Leaderboard", title, "User", title);
                    embed[0].setTitle(title);
                    embed[0].setColor(Color.BLUE);
                }
            } catch (SQLException e) {
                logger.error("Failed to get the uno leaderboard", e);
            }

        });

        if (counter[0] > 0) {
            embed[0].setDescription(desc[0]);
            embed[0].setImage("attachment://chart" + chartCounter[0] + ".png");
            chartCounter[0]++;
            charts.add(chart[0].createBarChart());
            embeds.add(embed[0].build());
            embed[0] = new EmbedBuilder();
            desc[0] = new StringBuilder();
            chart[0] = new BarChart("Uno Leaderboard", title, "User", title);
            embed[0].setTitle(title);
            embed[0].setColor(Color.BLUE);
        }

        if (ran.get()) {
            PaginatorWithImages paginator = new PaginatorWithImages(event.getUser(), embeds, (i, m) -> m.setFiles(FileUpload.fromData(charts.get(i), "chart" + i + ".png")));

            event.getHook().sendMessageEmbeds(paginator.currentPage()).addFiles(FileUpload.fromData(charts.get(0), "chart0.png"))
                    .addComponents(paginator.getActionRows()).queue(paginator::initialize);
        } else {
            event.getHook().sendMessage("No uno games in database.").queue();
        }
    }

}

