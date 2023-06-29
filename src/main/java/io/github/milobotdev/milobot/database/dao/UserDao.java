package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.model.User;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static UserDao instance = null;

    private UserDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table users ", e);
        }
    }

    public static synchronized UserDao getInstance() {
        if (instance == null) {
            instance = new UserDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "discord_id BIGINT NOT NULL UNIQUE," +
                "currency INT NOT NULL," +
                "level INT NOT NULL," +
                "experience INT NOT NULL" +
                ")";

        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void add(@NotNull Connection con, @NotNull User user) throws SQLException {
        String query = "INSERT INTO users (discord_id, currency, level, experience) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, user.getDiscordId());
            ps.setInt(2, user.getCurrency());
            ps.setInt(3, user.getLevel());
            ps.setInt(4, user.getExperience());
            ps.executeUpdate();
        }
    }

    public void delete(@NotNull Connection con, @NotNull User user) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
                ps.setLong(1,user.getDiscordId());
                ps.executeUpdate();
        }
    }

    public void update(@NotNull Connection con, @NotNull User user) throws SQLException {
        String query = "UPDATE users SET discord_id = ?, currency = ?, level = ?, experience = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, user.getDiscordId());
            ps.setInt(2, user.getCurrency());
            ps.setInt(3, user.getLevel());
            ps.setInt(4, user.getExperience());
            ps.setInt(5, user.getId());
            ps.executeUpdate();
        }
    }
    @Nullable
    public User getUserByDiscordId(@NotNull Connection con, long discordId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM users WHERE discord_id = ?");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, discordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new User(rs.getInt("id"), rs.getLong("discord_id"),
                            rs.getInt("currency"), rs.getInt("level"),
                            rs.getInt("experience"));
                } else {
                    return null;
                }
            }
        }
    }

    @Nullable
    public User getUserById(@NotNull Connection con, int id, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM users WHERE id = ?");
        PreparedStatement ps;
        ps = con.prepareStatement(query);
        ps.setInt(1, id);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new User(rs.getInt("id"), rs.getLong("discord_id"), rs.getInt("currency"),
                    rs.getInt("level"), rs.getInt("experience"));
        } else {
            return null;
        }
    }

    public int getUserExperienceRank(@NotNull Connection con, int userId) throws SQLException {
        String query =
                """
                        SELECT
                            `rank`
                        FROM
                            (SELECT
                                 id,
                                 RANK() OVER (ORDER BY experience DESC) `rank`
                             FROM users) AS user_ranks
                        WHERE id=?""";
        PreparedStatement ps;
        ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("rank");
        } else {
            throw new IllegalArgumentException("User with id " + userId + " not found");
        }
    }

    public int getUserCurrencyRank(@NotNull Connection con, int userId) throws SQLException {
            String query =
                """
                        SELECT
                            `rank`
                        FROM
                            (SELECT
                                 id,
                                 RANK() OVER (ORDER BY currency DESC) `rank`
                             FROM users) AS user_ranks
                        WHERE id=?""";
        PreparedStatement ps;
        ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return rs.getInt("rank");
        } else {
            throw new IllegalArgumentException("User with id " + userId + " not found");
        } }

    public int getTotalUserCount(@NotNull Connection con, RowLockType rowLockType) throws SQLException {
        String query = rowLockType.getQueryWithLock("SELECT COUNT(*) FROM users");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    throw new SQLException("Error getting total user count");
                }
            }
        }
    }
}
