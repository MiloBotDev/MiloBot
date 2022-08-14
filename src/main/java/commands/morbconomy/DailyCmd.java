package commands.morbconomy;

import commands.Command;
import database.DatabaseManager;
import database.queries.DailiesTableQueries;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import newdb.dao.UserDao;
import newdb.dao.UserDaoImplementation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import static java.time.temporal.ChronoUnit.MINUTES;

public class DailyCmd extends Command implements MorbconomyCmd {

    private final DatabaseManager dbManager;
    private final Random random;
    private static final Logger logger = LoggerFactory.getLogger(DailyCmd.class);
    private final UserDao userDao = UserDaoImplementation.getInstance();

    public DailyCmd() {
        this.commandName = "daily";
        this.commandDescription = "Collect your daily reward.";
        this.dbManager = DatabaseManager.getInstance();
        this.random = new Random();
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
        String userId = user.getId();
        LocalDateTime currentTime = LocalDateTime.now();

        ArrayList<String> userDaily = dbManager.query(DailiesTableQueries.getUserDaily, DatabaseManager.QueryTypes.RETURN, userId);
        String lastDailyDate = userDaily.get(1);
        int streak = Integer.parseInt(userDaily.get(2));
        int totalClaimed = Integer.parseInt(userDaily.get(3)) + 1;
        if(lastDailyDate.equals("null")) {
            result.append(String.format("You claimed your first daily! You earn `%d` morbcoins.", reward));
            streak++;
        } else {
            LocalDateTime lastDailyTime = LocalDateTime.parse(lastDailyDate);
            long minutesSinceLastDaily = MINUTES.between(lastDailyTime, currentTime);
            if(minutesSinceLastDaily < 1440) {
                // 24 hours haven't passed yet
                long minutesTillNextDaily = 1440 - minutesSinceLastDaily;
                long waitHours = Math.floorDiv(minutesTillNextDaily, 60);
                long waitMinutes = minutesTillNextDaily % 60;
                String waitTime;
                if(waitHours == 0) {
                    waitTime = String.format("%d minute(s)", waitMinutes);
                } else {
                   waitTime = String.format("%d hours and %d minute(s)", waitHours, waitMinutes);
                }
                result.append(String.format("You can claim your next daily in %s.", waitTime));
                return result.toString();
            } else if(minutesSinceLastDaily > 2880) {
                // it's been 48 hours so you lose your streak
                result.append(String.format("You earn `%d` morbcoins. Sadly you lost your streak of `%d` day(s).", reward, streak));
                streak = 1;
            } else {
                // you keep your streak
                streak++;
                if(streak == 1) {
                    result.append(String.format("You earn `%d` morbcoins.", reward));
                } else {
                    int bonusMorbcoins = streak * random.nextInt(100) + 25;
                    result.append(String.format("You earn `%d` morboins plus an additional `%d` morbcoins for being on a " +
                            "streak of `%d` day(s).", reward, bonusMorbcoins, streak));
                    reward += bonusMorbcoins;
                }
            }
        }
        dbManager.query(DailiesTableQueries.updateUserDaily, DatabaseManager.QueryTypes.UPDATE,
                currentTime.toString(), String.valueOf(streak), String.valueOf(totalClaimed), userId);

        newdb.model.User userDbObj = userDao.getUserByDiscordId(user.getIdLong());
        Objects.requireNonNull(userDbObj).setCurrency(userDbObj.getCurrency() + reward);
        userDao.update(userDbObj);
        return result.toString();
    }

}
