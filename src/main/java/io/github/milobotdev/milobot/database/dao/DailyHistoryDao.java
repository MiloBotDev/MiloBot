package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.model.DailyHistory;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DailyHistoryDao {

    private static final Logger logger = LoggerFactory.getLogger(DailyDao.class);
    private static DailyHistoryDao instance = null;

    private DailyHistoryDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating daily history table ", e);
        }
    }

    public static DailyHistoryDao getInstance() {
        if (instance == null) {
            instance = new DailyHistoryDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS daily_history (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "time DATETIME," +
                "amount INT NOT NULL" +
                ")";
        String addForeignKey = "ALTER TABLE daily_history " +
                "ADD FOREIGN KEY IF NOT EXISTS (user_id) REFERENCES users(id)";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
            st.execute(addForeignKey);
        }
    }

    public void add(Connection con, @NotNull DailyHistory dailyHistory) throws SQLException {
        String query = "INSERT INTO daily_history (user_id, time, amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, dailyHistory.getUserId());
            if (dailyHistory.getTime() == null) {
                ps.setNull(2, Types.DATE);
            } else {
                ps.setTimestamp(2, Timestamp.from(dailyHistory.getTime()));
            }
            ps.setInt(3, dailyHistory.getAmount());
            ps.executeUpdate();
        }
    }

    public List<DailyHistory> getLastDailyHistoryByUserDiscordId(Connection con, long userDiscordId, int limit) throws SQLException {
        String query = "SELECT * FROM daily_history WHERE user_id = (SELECT id FROM users WHERE discord_id = ?) ORDER BY time DESC LIMIT ?";
        List<DailyHistory> dailyHistories = new ArrayList<>();
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    dailyHistories.add(new DailyHistory(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getTimestamp("time").toInstant(),
                            rs.getInt("amount")
                    ));
                }
            }
        }
        return dailyHistories;
    }
}
