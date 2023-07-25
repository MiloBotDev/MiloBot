package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.WordleDao;
import io.github.milobotdev.milobot.database.model.Wordle;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.chart.PieChart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
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
        generateEmbed(event.getAuthor(), event.getChannel());
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        generateEmbed(event.getUser(), event.getChannel());
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(new SubcommandData("stats", "View your own wordle statistics"));
    }

    private void generateEmbed(User user, MessageChannel channel) {
        try(Connection con = DatabaseConnection.getConnection()) {
            EmbedBuilder embed = new EmbedBuilder();
            EmbedUtils.styleEmbed(embed, user);
            embed.setTitle(String.format("Wordle Statistics for %s", user.getName()));

            int id = Objects.requireNonNull(userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE)).getId();
            Wordle userWordle = wordleDao.getByUserId(con, id, RowLockType.NONE);
            con.commit();
            if (userWordle == null) {
                embed.setDescription("No wordle statistics on record.");
                channel.sendMessageEmbeds(embed.build())
                        .setActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
            } else {
                int fastestTime = userWordle.getFastestTime();
                int currentStreak = userWordle.getCurrentStreak();
                int totalGames = userWordle.getGamesPlayed();
                int highestStreak = userWordle.getHighestStreak();
                int totalWins = userWordle.getTotalWins();
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

                PieChart pieChart = new PieChart("Wins / Losses", user.getId());
                pieChart.addSection("Wins", totalWins, Color.GREEN);
                pieChart.addSection("Losses", totalLosses, Color.RED);
                embed.setImage("attachment://chart.png");
                channel.sendMessageEmbeds(embed.build()).addFiles(FileUpload.fromData(pieChart.createCircleDiagram(), "chart.png"))
                        .setActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
            }
        } catch (SQLException | IOException e) {
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("An error occurred while fetching your wordle statistics.");
            channel.sendMessageEmbeds(errorEmbed.build()).setActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
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
