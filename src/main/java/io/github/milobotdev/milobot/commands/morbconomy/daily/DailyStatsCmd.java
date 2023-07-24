package io.github.milobotdev.milobot.commands.morbconomy.daily;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.DailyDao;
import io.github.milobotdev.milobot.database.dao.DailyHistoryDao;
import io.github.milobotdev.milobot.database.model.Daily;
import io.github.milobotdev.milobot.database.model.DailyHistory;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.chart.LineChart;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class DailyStatsCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs {

    private final ExecutorService executorService;
    private final DailyDao dailyDao = DailyDao.getInstance();
    private final DailyHistoryDao dailyHistoryDao = DailyHistoryDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(DailyStatsCmd.class);

    public DailyStatsCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("stats", "View your own daily statistics.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        generateEmbed(event.getAuthor(), event.getChannel());
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        generateEmbed(event.getUser(), event.getChannel());
    }

    private void generateEmbed(User user, MessageChannel channel) {
        EmbedBuilder dailyStatsEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(dailyStatsEmbed, user);
        dailyStatsEmbed.setTitle(String.format("Daily Statistics for %s", user.getName()));

        try (Connection con = DatabaseConnection.getConnection()) {
            Daily daily = dailyDao.getDailyByUserDiscordId(con, user.getIdLong(), RowLockType.NONE);
            if(daily != null) {
                int streak = daily.getStreak();
                int highestStreak = daily.getHighestStreak();
                int totalClaimed = daily.getTotalClaimed();
                int highestCurrencyClaimed = daily.getHighestCurrencyClaimed();
                int totalCurrencyClaimed = daily.getTotalCurrencyClaimed();
                int lowestCurrencyClaimed = daily.getLowestCurrencyClaimed();
                int averageClaim = totalCurrencyClaimed / totalClaimed;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
                        .withZone(ZoneId.systemDefault());
                String lastDaily = formatter.format(Objects.requireNonNull(daily.getLastDailyTime()));
                dailyStatsEmbed.addField("Current Streak", String.valueOf(streak), true);
                dailyStatsEmbed.addField("Highest Streak", String.valueOf(highestStreak), true);
                dailyStatsEmbed.addField("Total Dailies Claimed", String.valueOf(totalClaimed), true);
                dailyStatsEmbed.addField("Last Daily Claimed", lastDaily, true);
                dailyStatsEmbed.addField("Total Morbcoins Claimed", String.valueOf(totalCurrencyClaimed), true);
                dailyStatsEmbed.addField("Highest Morbcoins Claim", String.valueOf(highestCurrencyClaimed), true);
                dailyStatsEmbed.addField("Lowest Morbcoins Claim", String.valueOf(lowestCurrencyClaimed), true);
                dailyStatsEmbed.addField("Average Morbcoins Claim", String.valueOf(averageClaim), true);

                List<DailyHistory> dailyHistory = dailyHistoryDao.getLastDailyHistoryByUserDiscordId(con, user.getIdLong(), 10);
                // reverse the dailyhistory list so its in the right order for the chart
                dailyHistory.sort(Comparator.comparing(DailyHistory::getTime));
                if(!dailyHistory.isEmpty()) {
                    LineChart lineChart = new LineChart("Daily History", "Date", "Total Morbcoins Claimed");
                    dailyHistory.forEach(dailyHistoryEntry -> lineChart.addPlotPoint(dailyHistoryEntry.getAmount(),
                            "Total Morbcoins Claimed",  formatter.format(dailyHistoryEntry.getTime())));
                    dailyStatsEmbed.setImage("attachment://dailyHistory.png");
                    channel.sendMessageEmbeds(dailyStatsEmbed.build())
                            .addFile(lineChart.createLineChart(), "dailyHistory.png")
                            .setActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
                    return;
                }
            } else {
                dailyStatsEmbed.setDescription("No Daily statistics on record.");
            }
        } catch (SQLException e) {
            logger.error("SQL Error while generating embed for user " + user.getId(), e);
        }
        channel.sendMessageEmbeds(dailyStatsEmbed.build()).setActionRow(
                Button.secondary(user.getId() + ":delete", "Delete")).queue();
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
