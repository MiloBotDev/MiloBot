package commands.morbconomy.daily;

import commands.Command;
import commands.SubCmd;
import commands.games.blackjack.BlackjackStatsCmd;
import database.dao.DailyDao;
import database.model.Daily;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EmbedUtils;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;

public class DailyStatsCmd extends Command implements SubCmd {

    private final DailyDao dailyDao = DailyDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(DailyStatsCmd.class);

    public DailyStatsCmd() {
        this.commandName = "stats";
        this.commandDescription = "View your own daily statistics.";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        event.getChannel().sendMessageEmbeds(generateEmbed(event.getAuthor()).build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.replyEmbeds(generateEmbed(event.getUser()).build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }

    private @NotNull EmbedBuilder generateEmbed(User user) {
        EmbedBuilder dailyStatsEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(dailyStatsEmbed, user);
        dailyStatsEmbed.setTitle(String.format("Daily Statistics for %s", user.getName()));

        try {
            Daily daily = dailyDao.getDailyByUserDiscordId(user.getIdLong());
            if(daily != null) {
                int streak = daily.getStreak();
                int totalClaimed = daily.getTotalClaimed();
                String lastDailyTime = daily.getLastDailyTime().toString();

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
}