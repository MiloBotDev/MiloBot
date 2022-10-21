package commands.morbconomy.daily;

import commands.Command;
import commands.SubCmd;
import database.dao.DailyDao;
import database.model.Daily;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class DailyStreakCmd extends Command implements SubCmd {

    private final DailyDao dailyDao = DailyDao.getInstance();

    public DailyStreakCmd() {
        this.commandName = "streak";
        this.commandDescription = "View your current streak.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        event.getChannel().sendMessage(String.format("You are on a streak of `%d` days.",
                getStreak(event.getAuthor().getIdLong()))).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.reply(String.format("You are on a streak of `%d` days.",
                getStreak(event.getUser().getIdLong()))).queue();
    }

    private int getStreak(long userId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            Daily daily = dailyDao.getDailyByUserDiscordId(con, userId, RowLockType.NONE);
            if (daily != null) {
                return daily.getStreak();
            } else {
                return 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
