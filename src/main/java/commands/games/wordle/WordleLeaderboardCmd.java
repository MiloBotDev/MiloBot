package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.dao.UserDao;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import database.model.Wordle;
import net.dv8tion.jda.api.interactions.components.selections.SelectionMenu;
import org.jetbrains.annotations.NotNull;
import utility.Users;

import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * View all the leaderboards for the Wordle command.
 */
public class WordleLeaderboardCmd extends Command implements SubCmd {

    private static final UserDao userDao = UserDao.getInstance();
    private static final Users userUtil = Users.getInstance();

    public WordleLeaderboardCmd() {
        this.commandName = "leaderboard";
        this.commandDescription = "View the wordle leaderboards.";
        this.commandArgs = new String[]{};
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
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        SelectionMenu menu = SelectionMenu.create(event.getUser().getId() + ":wordleLeaderboard")
                .setPlaceholder("Select a leaderboard")
                .addOption("Highest Streak", "highestStreak")
                .addOption("Fastest Time", "fastestTime")
                .addOption("Total Wins", "totalWins")
                .addOption("Total Games Played", "totalGames")
                .addOption("Current Streak", "currentStreak")
                .addOption("Highest Streak", "highestStreak")
                .build();
        event.reply("Wordle Leaderboard Selection").addActionRow(menu).queue();
    }

    public static @NotNull ArrayList<MessageEmbed> buildWordleEmbeds(@NotNull List<Wordle> wordles, String title, JDA jda) {
        ArrayList<MessageEmbed> embeds = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        embed[0].setTitle(title);
        embed[0].setColor(Color.BLUE);

        final int[] counter = {1};
        wordles.forEach((wordle) -> {
            try(Connection con = DatabaseConnection.getConnection()) {
                long discordId = Objects.requireNonNull(userDao.getUserById(con, wordle.getUserId(), RowLockType.NONE)).getDiscordId();
                String name = userUtil.getUserNameTag(discordId, jda).userName();
                switch (title) {
                    case "Highest Streak" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, wordle.getHighestStreak()));
                    case "Fastest Time" -> desc[0].append(String.format("`%d.` %s - %d seconds.\n", counter[0], name, wordle.getFastestTime()));
                    case "Total Wins" -> desc[0].append(String.format("`%d.` %s - %d wins.\n", counter[0], name, wordle.getWins()));
                    case "Total Games Played" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, wordle.getGamesPlayed()));
                    case "Current Streak" -> desc[0].append(String.format("`%d.` %s - %d games.\n", counter[0], name, wordle.getCurrentStreak()));
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

