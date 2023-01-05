package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.model.Daily;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DailyDao {

    private static final Logger logger = LoggerFactory.getLogger(DailyDao.class);
    private static DailyDao instance = null;

    private DailyDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table daily ", e);
        }
    }

    public static synchronized DailyDao getInstance() {
        if (instance == null) {
            instance = new DailyDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        UserDao.getInstance();
        String query = "CREATE TABLE IF NOT EXISTS daily (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "last_daily_time DATETIME," +
                "streak INT NOT NULL," +
                "total_claimed INT NOT NULL" +
                ")";
        String addHighestStreak = "ALTER TABLE daily " +
                "ADD IF NOT EXISTS highest_streak INT NOT NULL," +
                "ADD IF NOT EXISTS total_currency_claimed INT NOT NULL," +
                "ADD IF NOT EXISTS highest_currency_claimed INT NOT NULL," +
                "ADD IF NOT EXISTS lowest_currency_claimed INT NOT NULL";
        String addForeignKeyConstraint = "ALTER TABLE daily " +
                "ADD FOREIGN KEY IF NOT EXISTS (user_id) REFERENCES users(id)";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
                st.execute(query);
                st.execute(addHighestStreak);
                st.execute(addForeignKeyConstraint);
        }
    }

    public void add(@NotNull Connection con, @NotNull Daily daily) throws SQLException {
        String query = "INSERT INTO daily (user_id, last_daily_time, streak, total_claimed, highest_streak, total_currency_claimed, " +
                "highest_currency_claimed, lowest_currency_claimed) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, daily.getUserId());
            if (daily.getLastDailyTime() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setTimestamp(2, Timestamp.from(daily.getLastDailyTime()));
            }
            ps.setInt(3, daily.getStreak());
            ps.setInt(4, daily.getTotalClaimed());
            ps.setInt(5, daily.getHighestStreak());
            ps.setInt(6, daily.getTotalCurrencyClaimed());
            ps.setInt(7, daily.getHighestCurrencyClaimed());
            ps.setInt(8, daily.getLowestCurrencyClaimed());
            ps.executeUpdate();
        }
    }

    public void update(@NotNull Connection con, @NotNull Daily daily) throws SQLException {
        String query = "UPDATE daily SET user_id = ?, last_daily_time = ?, streak = ?, total_claimed = ?, highest_streak = ?, " +
                "total_currency_claimed = ?, highest_currency_claimed = ?, lowest_currency_claimed  = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, daily.getUserId());
            if (daily.getLastDailyTime() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setTimestamp(2, Timestamp.from(daily.getLastDailyTime()));
            }
            ps.setInt(3, daily.getStreak());
            ps.setInt(4, daily.getTotalClaimed());
            ps.setInt(5, daily.getHighestStreak());
            ps.setInt(6, daily.getTotalCurrencyClaimed());
            ps.setInt(7, daily.getHighestCurrencyClaimed());
            ps.setInt(8, daily.getLowestCurrencyClaimed());
            ps.setInt(9, daily.getId());
            ps.executeUpdate();
        }
    }

    @Nullable
    public Daily getDailyByUserDiscordId(@NotNull Connection con, long userDiscordId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM daily WHERE user_id = (SELECT id FROM users WHERE discord_id = ?)");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Daily(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("last_daily_time") != null ? rs.getTimestamp("last_daily_time").toInstant() : null,
                            rs.getInt("streak"),
                            rs.getInt("total_claimed"),
                            rs.getInt("highest_streak"),
                            rs.getInt("total_currency_claimed"),
                            rs.getInt("highest_currency_claimed"),
                            rs.getInt("lowest_currency_claimed")
                    );
                } else {
                    return null;
                }
            }
        }
    }

    @Nullable
    public Daily getDailyByUserId(@NotNull Connection con, long userId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM daily WHERE user_id = ?");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Daily(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("last_daily_time") != null ? rs.getTimestamp("last_daily_time").toInstant() : null,
                            rs.getInt("streak"),
                            rs.getInt("total_claimed"),
                            rs.getInt("highest_streak"),
                            rs.getInt("total_currency_claimed"),
                            rs.getInt("highest_currency_claimed"),
                            rs.getInt("lowest_currency_claimed")
                    );
                } else {
                    return null;
                }
            }
        }
    }
}
