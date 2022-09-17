package commands.morbconomy.daily;

import commands.Command;
import commands.SubCmd;
import database.dao.DailyDao;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

public class DailyStreakCmd extends Command implements SubCmd {

    private final DailyDao dailyDao = DailyDao.getInstance();

    public DailyStreakCmd() {
        this.commandName = "streak";
        this.commandDescription = "View your current streak.";
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
        try {
            return dailyDao.getDailyByUserDiscordId(userId).getStreak();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
