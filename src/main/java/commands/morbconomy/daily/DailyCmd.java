package commands.morbconomy.daily;

import commands.Command;
import commands.ParentCmd;
import commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import database.dao.DailyDao;
import database.dao.UserDao;
import database.model.Daily;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.time.temporal.ChronoUnit.MINUTES;

public class DailyCmd extends Command implements MorbconomyCmd, ParentCmd {

    private static final Logger logger = LoggerFactory.getLogger(DailyCmd.class);
    private final Random random = new Random();
    private final UserDao userDao = UserDao.getInstance();
    private final DailyDao dailyDao = DailyDao.getInstance();

    public DailyCmd() {
        this.commandName = "daily";
        this.commandDescription = "Collect your daily reward.";
        this.subCommands.add(new DailyStreakCmd());
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        try {
            String s = updateDailies(event.getAuthor());
            event.getChannel().sendMessage(s).queue();
        } catch (SQLException e) {
            logger.error("Error updating dailies", e);
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        try {
            String s = updateDailies(event.getUser());
            event.getHook().sendMessage(s).queue();
        } catch (SQLException e) {
            logger.error("Error updating dailies", e);
            event.getHook().sendMessage("Internal error. Please try again later.").queue();
        }
    }

    private @NotNull String updateDailies(@NotNull User user) throws SQLException {
        int reward = random.nextInt(5000) + 1000;

        StringBuilder result = new StringBuilder();

        Instant timeNow = Instant.now();
        Daily daily = Objects.requireNonNull(dailyDao.getDailyByUserDiscordId(user.getIdLong()));
        daily.incrementTotalClaimed();
        if (daily.getLastDailyTime() == null) {
            result.append(String.format("You claimed your first daily! You earn `%d` morbcoins.", reward));
            daily.incrementStreak();
        } else {
            long minutesSinceLastDaily = MINUTES.between(daily.getLastDailyTime(), timeNow);
            if (minutesSinceLastDaily < 1440) {
                // 24 hours haven't passed yet
                long minutesTillNextDaily = 1440 - minutesSinceLastDaily;
                long waitHours = Math.floorDiv(minutesTillNextDaily, 60);
                long waitMinutes = minutesTillNextDaily % 60;
                String waitTime;
                if (waitHours == 0) {
                    waitTime = String.format("%d minute(s)", waitMinutes);
                } else {
                    waitTime = String.format("%d hours and %d minute(s)", waitHours, waitMinutes);
                }
                result.append(String.format("You can claim your next daily in %s.", waitTime));
                return result.toString();
            } else if (minutesSinceLastDaily > 2880) {
                // it's been 48 hours so you lose your streak
                result.append(String.format("You earn `%d` morbcoins. Sadly you lost your streak of `%d` day(s).", reward, daily.getStreak()));
                daily.resetStreak();
            } else {
                // you keep your streak
                daily.incrementStreak();
                if (daily.getStreak() == 1) {
                    result.append(String.format("You earn `%d` morbcoins.", reward));
                } else {
                    int bonusMorbcoins = daily.getStreak() * random.nextInt(100) + 25;
                    result.append(String.format("You earn `%d` morboins plus an additional `%d` morbcoins for being on a " +
                            "streak of `%d` day(s).", reward, bonusMorbcoins, daily.getStreak()));
                    reward += bonusMorbcoins;
                }
            }
        }
        daily.setLastDailyTime(timeNow);
        dailyDao.update(daily);

        database.model.User userDbObj = userDao.getUserByDiscordId(user.getIdLong());
        Objects.requireNonNull(userDbObj).setCurrency(userDbObj.getCurrency() + reward);
        userDao.update(userDbObj);
        return result.toString();
    }

}
