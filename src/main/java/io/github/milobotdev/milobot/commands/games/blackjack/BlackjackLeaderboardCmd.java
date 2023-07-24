package io.github.milobotdev.milobot.commands.games.blackjack;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.BlackjackDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.Blackjack;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.Users;
import io.github.milobotdev.milobot.utility.paginator.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BlackjackLeaderboardCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs, EventListeners {

    private final ExecutorService executorService;
    private static final UserDao userDao = UserDao.getInstance();
    private static final BlackjackDao blackjackDao = BlackjackDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BlackjackLeaderboardCmd.class);
    private static final Users userUtil = Users.getInstance();

    public BlackjackLeaderboardCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        SelectionMenu menu = SelectionMenu.create(event.getUser().getId() + ":blackjackLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Draws", "totalDraws")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Earnings", "totalEarnings")
                .build();
        event.reply("Blackjack Leaderboard Selection").addActionRow(menu).queue();
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        SelectionMenu menu = SelectionMenu.create(event.getAuthor().getId() + ":blackjackLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Current Streak", "currentStreak")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Draws", "totalDraws")
                .addOption("Total Games Played", "totalGamesPlayed")
                .addOption("Total Earnings", "totalEarnings")
                .build();
        event.getChannel().sendMessage("Blackjack Leaderboard Selection").setActionRow(menu).queue();
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("leaderboard","View the blackjack leaderboards.");
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull List<EventListener> getEventListeners() {
        return List.of(menuListener);
    }

    ListenerAdapter menuListener = new ListenerAdapter() {
        @Override
        public void onSelectionMenu(@NotNull SelectionMenuEvent event) {
            String[] id = event.getComponentId().split(":");
            String authorId = id[0];
            String type = id[1];
            User user = event.getUser();

            if (user.getId().equals(authorId) && type.equals("blackjackLeaderboard")) {
                event.deferEdit().queue();
                String option = Objects.requireNonNull(event.getSelectedOptions()).get(0).getValue();
                List<MessageEmbed> embeds = new ArrayList<>();
                try {
                    switch (option) {
                        case "totalWins" -> {
                            List<Blackjack> topTotalWins = blackjackDao.getBlackjacks(BlackjackDao.BlackjackLeaderboardType.TOTAL_WINS);
                            embeds = buildBlackjackEmbeds(topTotalWins, "Total Wins", event.getJDA());
                        }
                        case "totalDraws" -> {
                            List<Blackjack> topTotalDraws = blackjackDao.getBlackjacks(BlackjackDao.BlackjackLeaderboardType.TOTAL_DRAWS);
                            embeds = buildBlackjackEmbeds(topTotalDraws, "Total Draws", event.getJDA());
                        }
                        case "totalGamesPlayed" -> {
                            List<Blackjack> topTotalGamesPlayed = blackjackDao.getBlackjacks(BlackjackDao.BlackjackLeaderboardType.TOTAL_GAMES_PLAYED);
                            embeds = buildBlackjackEmbeds(topTotalGamesPlayed, "Total Games Played", event.getJDA());
                        }
                        case "highestStreak" -> {
                            List<Blackjack> topHighestStreak = blackjackDao.getBlackjacks(BlackjackDao.BlackjackLeaderboardType.HIGHEST_STREAK);
                            embeds = buildBlackjackEmbeds(topHighestStreak, "Highest Streak", event.getJDA());
                        }
                        case "currentStreak" -> {
                            List<Blackjack> topCurrentStreak = blackjackDao.getBlackjacks(BlackjackDao.BlackjackLeaderboardType.CURRENT_STREAK);
                            embeds = buildBlackjackEmbeds(topCurrentStreak, "Current Streak", event.getJDA());
                        }
                        case "totalEarnings" -> {
                            List<Blackjack> topTotalEarnings = blackjackDao.getBlackjacks(BlackjackDao.BlackjackLeaderboardType.TOTAL_EARNINGS);
                            embeds = buildBlackjackEmbeds(topTotalEarnings, "Total Earnings", event.getJDA());
                        }
                    }
                    Paginator paginator = new Paginator(user, embeds);
                    event.getHook().sendMessageEmbeds(paginator.currentPage()).addActionRows(paginator.getActionRows())
                            .queue(paginator::initialize);
                } catch (SQLException e) {
                    logger.error("Failed to get the blackjack leaderboard", e);
                }
            }
        }
    };

    public static @NotNull ArrayList<MessageEmbed> buildBlackjackEmbeds(@NotNull List<Blackjack> blackjacks, String title, JDA jda) {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] counter = {1};
        blackjacks.forEach((blackjack) -> {
            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                long discordId = Objects.requireNonNull(userDao.getUserById(con, blackjack.getUserId(), RowLockType.NONE)).getDiscordId();
                con.commit();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Highest Streak" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, blackjack.getHighestStreak()));
                    case "Current Streak" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, blackjack.getStreak()));
                    case "Total Wins" -> desc[0].append(String.format("`%d.` %s - %d wins.\n", counter[0], name, blackjack.getTotalWins()));
                    case "Total Draws" -> desc[0].append(String.format("`%d.` %s - %d draws.\n", counter[0], name, blackjack.getTotalDraws()));
                    case "Total Games Played" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, blackjack.getTotalGames()));
                    case "Total Earnings" -> desc[0].append(String.format("`%d.` %s - %d morbcoins.\n", counter[0], name, blackjack.getTotalEarnings()));
                }
                counter[0]++;
                if(counter[0] % 10 == 0) {
                    embed[0].setDescription(desc[0]);
                    embeds.add(embed[0].build());
                    embed[0] = new EmbedBuilder();
                    desc[0] = new StringBuilder();
                    embed[0].setTitle(title);
                    embed[0].setColor(Color.BLUE);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });

        embed[0].setDescription(desc[0]);
        embeds.add(embed[0].build());

        return embeds;
    }


}
