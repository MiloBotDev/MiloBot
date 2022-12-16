package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.WordleDao;
import io.github.milobotdev.milobot.database.model.Wordle;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class WordleStatsCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes {

    private final ExecutorService executorService;
    private final WordleDao wordleDao;
    private final UserDao userDao;

    public WordleStatsCmd(ExecutorService executorService) {
        this.executorService = executorService;
        this.wordleDao = WordleDao.getInstance();
        this.userDao = UserDao.getInstance();
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        EmbedBuilder embedBuilder = generateEmbed(event.getAuthor());
        event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        EmbedBuilder embedBuilder = generateEmbed(event.getUser());
        event.replyEmbeds(embedBuilder.build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("stats", "View your own wordle statistics");
    }

    private @NotNull EmbedBuilder generateEmbed(User user) {
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            EmbedBuilder embed = new EmbedBuilder();
            EmbedUtils.styleEmbed(embed, user);
            embed.setTitle(String.format("Wordle Statistics for %s", user.getName()));

            int id = Objects.requireNonNull(userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE)).getId();
            Wordle userWordle = wordleDao.getByUserId(con, id, RowLockType.NONE);
            con.commit();
            if (userWordle == null) {
                embed.setDescription("No wordle statistics on record.");
            } else {
                int fastestTime = userWordle.getFastestTime();
                int currentStreak = userWordle.getCurrentStreak();
                int totalGames = userWordle.getGamesPlayed();
                int highestStreak = userWordle.getHighestStreak();
                int totalWins = userWordle.getWins();
                int totalLosses = totalGames - totalWins;

                double winRate = (double) totalWins / totalGames;
                int roundedWinRate = (int) Math.ceil(winRate * 100);
                String winRateString = roundedWinRate + "%";

                embed.addField("Total Games", String.valueOf(totalGames), true);
                embed.addField("Total Wins", String.valueOf(totalWins), true);
                embed.addField("Total Losses", String.valueOf(totalLosses), true);
                embed.addField("Win Rate", winRateString, true);
                embed.addField("Current Streak", String.valueOf(currentStreak), true);
                embed.addField("Highest Streak", String.valueOf(highestStreak), true);

                if (fastestTime != 0) {
                    embed.addField("Fastest Time", fastestTime + " Seconds", true);
                } else {
                    embed.addField("Fastest Time", "None", true);
                }
            }
            return embed;
        } catch (SQLException e) {
            return new EmbedBuilder().setTitle("Error").setDescription("An error occurred while fetching your wordle statistics.");
        }
    }


    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }
}
