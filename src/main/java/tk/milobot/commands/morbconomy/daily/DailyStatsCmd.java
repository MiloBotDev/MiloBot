package tk.milobot.commands.morbconomy.daily;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.commands.command.SubCommand;
import tk.milobot.commands.command.extensions.*;
import tk.milobot.database.dao.DailyDao;
import tk.milobot.database.model.Daily;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import tk.milobot.utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class DailyStatsCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs  {

    private final ExecutorService executorService;
    private final DailyDao dailyDao = DailyDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(DailyStatsCmd.class);

    public DailyStatsCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("stats", "View your own daily statistics.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        event.getChannel().sendMessageEmbeds(generateEmbed(event.getAuthor()).build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        event.replyEmbeds(generateEmbed(event.getUser()).build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }

    private @NotNull EmbedBuilder generateEmbed(User user) {
        EmbedBuilder dailyStatsEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(dailyStatsEmbed, user);
        dailyStatsEmbed.setTitle(String.format("Daily Statistics for %s", user.getName()));

        try (Connection con = DatabaseConnection.getConnection()) {
            Daily daily = dailyDao.getDailyByUserDiscordId(con, user.getIdLong(), RowLockType.NONE);
            if(daily != null) {
                int streak = daily.getStreak();
                int totalClaimed = daily.getTotalClaimed();
                String lastDailyTime = Objects.requireNonNull(daily.getLastDailyTime()).toString();
                dailyStatsEmbed.addField("Current Streak", String.valueOf(streak), true);
                dailyStatsEmbed.addField("Total Dailies Claimed", String.valueOf(totalClaimed), true);
                dailyStatsEmbed.addField("Last Daily Claimed", lastDailyTime, true);
            } else {
                dailyStatsEmbed.setDescription("No Daily statistics on record.");
            }
        } catch (SQLException e) {
            logger.error("SQL Error while generating embed for user " + user.getId(), e);
        }

        return dailyStatsEmbed;
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
