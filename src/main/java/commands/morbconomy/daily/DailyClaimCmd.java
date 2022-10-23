package commands.morbconomy.daily;

import commands.Command;
import commands.SubCmd;
import database.dao.DailyDao;
import database.dao.UserDao;
import database.model.Daily;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.time.temporal.ChronoUnit.MINUTES;

public class DailyClaimCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(DailyCmd.class);
    private final Random random = new Random();
    private final UserDao userDao = UserDao.getInstance();
    private final DailyDao dailyDao = DailyDao.getInstance();

    public DailyClaimCmd() {
        this.commandName = "claim";
        this.commandDescription = "Claim your daily reward.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
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
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            database.model.User userDbObj = Objects.requireNonNull(
                    userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE));
            Daily daily = dailyDao.getDailyByUserId(con, userDbObj.getId(), RowLockType.FOR_UPDATE);
            if (daily == null) {
                daily = new Daily(userDbObj.getId());
                dailyDao.add(con, daily);
            }

            boolean claimed = true;
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
                    claimed = false;
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

            if (claimed) {
                daily.incrementTotalClaimed();
                daily.setLastDailyTime(timeNow);
                dailyDao.update(con, daily);

                userDbObj.setCurrency(userDbObj.getCurrency() + reward);
                userDao.update(con, userDbObj);
            }
            con.commit();
        }

        return result.toString();
    }
}
