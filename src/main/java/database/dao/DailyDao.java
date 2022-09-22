package database.dao;

import database.model.Daily;
import database.util.DatabaseConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class DailyDao {
    private static final Connection con = DatabaseConnection.getConnection();
    private static final Logger logger = LoggerFactory.getLogger(DailyDao.class);
    private static DailyDao instance = null;

    private DailyDao() {
        try {
            creteTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table daily ", e);
        }
    }

    public static DailyDao getInstance() {
        if (instance == null) {
            instance = new DailyDao();
        }
        return instance;
    }

    private void creteTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS daily (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "last_daily_time DATETIME," +
                "streak INT NOT NULL," +
                "total_claimed INT NOT NULL" +
                ")";
        Statement st = con.createStatement();
        st.execute(query);
    }

    public void add(@NotNull Daily daily) throws SQLException {
        String query = "INSERT INTO daily (user_id, last_daily_time, streak, total_claimed) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, daily.getUserId());
        if (daily.getLastDailyTime() == null) {
            ps.setNull(2, Types.DATE);
        } else {
            ps.setTimestamp(2, Timestamp.from(daily.getLastDailyTime()));
        }
        ps.setInt(3, daily.getStreak());
        ps.setInt(4, daily.getTotalClaimed());
        ps.executeUpdate();
    }

    public void update(@NotNull Daily daily) throws SQLException {
        String query = "UPDATE daily SET user_id = ?, last_daily_time = ?, streak = ?, total_claimed = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, daily.getUserId());
        if (daily.getLastDailyTime() == null) {
            ps.setNull(2, Types.DATE);
        } else {
            ps.setTimestamp(2, Timestamp.from(daily.getLastDailyTime()));
        }
        ps.setInt(3, daily.getStreak());
        ps.setInt(4, daily.getTotalClaimed());
        ps.setInt(5, daily.getId());
        ps.executeUpdate();
    }

    @Nullable
    public Daily getDailyByUserDiscordId(long userDiscordId) throws SQLException {
        String query = "SELECT * FROM daily WHERE user_id = (SELECT id FROM users WHERE discord_id = ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, userDiscordId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Daily(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getTimestamp("last_daily_time") != null ? rs.getTimestamp("last_daily_time").toInstant() : null,
                    rs.getInt("streak"),
                    rs.getInt("total_claimed")
            );
        } else {
            return null;
        }
    }
}
