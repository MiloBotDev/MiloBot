package io.github.milobotdev.milobot.commands.games.uno;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.UnoDao;
import io.github.milobotdev.milobot.database.model.Uno;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.chart.PieChart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class UnoStatsCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes {

    private final ExecutorService executorService;
    private final UnoDao unoDao = UnoDao.getInstance();
    private static final Logger logger = LoggerFactory.getLogger(UnoStatsCmd.class);

    public UnoStatsCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("stats", "View your own uno statistics");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        generateStatsEmbed(event.getAuthor(), event.getChannel());
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        generateStatsEmbed(event.getUser(), event.getChannel());
    }

    private void generateStatsEmbed(User user, MessageChannel channel) {
        try {
            Connection con = DatabaseConnection.getConnection();
            EmbedBuilder unoStatsEmbed = new EmbedBuilder();
            EmbedUtils.styleEmbed(unoStatsEmbed, user);
            Optional<Uno> byUserDiscordId = this.unoDao.getByUserDiscordId(con, user.getIdLong(), RowLockType.NONE);
            byUserDiscordId.ifPresentOrElse(uno -> {
                int highestStreak = uno.getHighestStreak();
                int streak = uno.getStreak();
                int totalWins = uno.getTotalWins();
                int totalGamesPlayed = uno.getTotalGamesPlayed();
                int totalLosses = totalGamesPlayed - totalWins;
                double winRate = (double) totalWins / totalGamesPlayed;
                int roundedWinRate = (int) Math.ceil(winRate * 100);
                String winRateString = roundedWinRate + "%";
                int totalCardsDrawn = uno.getTotalCardsDrawn();
                int totalCardsPlayed = uno.getTotalCardsPlayed();

                unoStatsEmbed.addField("Total Games Played", String.valueOf(totalGamesPlayed), true);
                unoStatsEmbed.addField("Total Wins", String.valueOf(totalWins), true);
                unoStatsEmbed.addField("Total Losses", String.valueOf(totalLosses), true);
                unoStatsEmbed.addField("Winrate", winRateString, true);
                unoStatsEmbed.addField("Current Streak", String.valueOf(streak), true);
                unoStatsEmbed.addField("Highest Streak", String.valueOf(highestStreak), true);
                unoStatsEmbed.addField("Total Cards Played", String.valueOf(totalCardsPlayed), true);
                unoStatsEmbed.addField("Total Cards Drawn", String.valueOf(totalCardsDrawn), true);

                PieChart pieChart = new PieChart("Wins / Losses", user.getId());
                pieChart.addSection("Wins", totalWins, Color.GREEN);
                pieChart.addSection("Losses", totalLosses, Color.RED);
                unoStatsEmbed.setImage("attachment://chart.png");
                try {
                    channel.sendMessageEmbeds(unoStatsEmbed.build()).addFile(pieChart.createCircleDiagram(), "chart.png")
                            .setActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
                } catch (IOException e) {
                    logger.error("An error occured while making a pie chart", e);
                }
            }, () -> {
                unoStatsEmbed.setDescription("No uno statistics on record.");
                channel.sendMessageEmbeds(unoStatsEmbed.build()).setActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
            });
        } catch (SQLException e){
            logger.error("An error occured while loading uno statistics", e);
            EmbedBuilder errorEmbed = new EmbedBuilder()
                    .setTitle("Error")
                    .setDescription("An error occurred while fetching your uno statistics.");
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
