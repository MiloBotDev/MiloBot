package io.github.milobotdev.milobot.commands.morbconomy.daily;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.database.dao.DailyDao;
import io.github.milobotdev.milobot.database.dao.DailyHistoryDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.Daily;
import io.github.milobotdev.milobot.database.model.DailyHistory;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
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
import java.util.Set;
import java.util.concurrent.ExecutorService;

import static java.time.temporal.ChronoUnit.MINUTES;

public class DailyClaimCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs {

    private final ExecutorService executorService;
    private static final Logger logger = LoggerFactory.getLogger(DailyCmd.class);
    private final Random random = new Random();
    private final UserDao userDao = UserDao.getInstance();
    private final DailyDao dailyDao = DailyDao.getInstance();
    private final DailyHistoryDao dailyHistoryDao = DailyHistoryDao.getInstance();

    public DailyClaimCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(
                new SubcommandData("claim", "Claim your daily reward.")
        );
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        try {
            String s = updateDailies(event.getAuthor());
            event.getChannel().sendMessage(s).queue();
        } catch (SQLException e) {
            logger.error("Error updating dailies", e);
        }
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
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
            io.github.milobotdev.milobot.database.model.User userDbObj = Objects.requireNonNull(
                    userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE));
            int userId = userDbObj.getId();
            Daily daily = dailyDao.getDailyByUserId(con, userId, RowLockType.FOR_UPDATE);
            if (daily == null) {
                daily = new Daily(userId);
                dailyDao.add(con, daily);
                daily = dailyDao.getDailyByUserId(con, userId, RowLockType.FOR_UPDATE);
            }

            boolean claimed = true;
            if (Objects.requireNonNull(daily).getLastDailyTime() == null) {
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
                daily.incrementTotalClaimed(reward);
                daily.setLastDailyTime(timeNow);
                dailyDao.update(con, daily);
                dailyHistoryDao.add(con, new DailyHistory(userId, timeNow, reward));

                userDbObj.setCurrency(userDbObj.getCurrency() + reward);
                userDao.update(con, userDbObj);
            }
            con.commit();
        }

        return result.toString();
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
