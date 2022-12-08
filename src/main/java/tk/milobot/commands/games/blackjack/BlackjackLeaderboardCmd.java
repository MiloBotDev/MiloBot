package tk.milobot.commands.games.blackjack;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.database.dao.BlackjackDao;
import tk.milobot.database.dao.UserDao;
import tk.milobot.database.model.Blackjack;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SelectionMenuEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.Paginator;
import tk.milobot.utility.Users;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BlackjackLeaderboardCmd extends Command implements SubCmd {

    private static final UserDao userDao = UserDao.getInstance();
    private static final BlackjackDao blackjackDao = BlackjackDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(BlackjackLeaderboardCmd.class);
    private static final Users userUtil = Users.getInstance();

    public BlackjackLeaderboardCmd() {
        this.commandName = "leaderboard";
        this.commandDescription = "View the blackjack leaderboards.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);

        this.listeners.add(new ListenerAdapter() {
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
                                List<Blackjack> topTotalWins = blackjackDao.getTopTotalWins();
                                embeds = buildBlackjackEmbeds(topTotalWins, "Total Wins", event.getJDA());
                            }
                            case "totalDraws" -> {
                                List<Blackjack> topTotalDraws = blackjackDao.getTopTotalDraws();
                                embeds = buildBlackjackEmbeds(topTotalDraws, "Total Draws", event.getJDA());
                            }
                            case "totalGamesPlayed" -> {
                                List<Blackjack> topTotalGamesPlayed = blackjackDao.getTopTotalGamesPlayed();
                                embeds = buildBlackjackEmbeds(topTotalGamesPlayed, "Total Games Played", event.getJDA());
                            }
                            case "highestStreak" -> {
                                List<Blackjack> topHighestStreak = blackjackDao.getTopHighestStreak();
                                embeds = buildBlackjackEmbeds(topHighestStreak, "Highest Streak", event.getJDA());
                            }
                            case "currentStreak" -> {
                                List<Blackjack> topCurrentStreak = blackjackDao.getTopCurrentStreak();
                                embeds = buildBlackjackEmbeds(topCurrentStreak, "Current Streak", event.getJDA());
                            }
                            case "totalEarnings" -> {
                                List<Blackjack> topTotalEarnings = blackjackDao.getTopTotalEarnings();
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
        });
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
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
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