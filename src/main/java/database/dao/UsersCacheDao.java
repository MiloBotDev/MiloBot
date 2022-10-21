package database.dao;

import database.util.DatabaseConnection;
import database.util.RowLockType;
import models.UserNameTag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UsersCacheDao {

    private static final Logger logger = LoggerFactory.getLogger(UsersCacheDao.class);
    private static UsersCacheDao instance = null;

    private UsersCacheDao() {
        try {
            createTableIfNotExists();
            reset();
        } catch (SQLException e) {
            logger.error("Error creating and resetting table users_cache ", e);
        }
    }

    public static UsersCacheDao getInstance() {
        if (instance == null) {
            instance = new UsersCacheDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS users_cache (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "username VARCHAR(32) NOT NULL," +
                "discriminator SMALLINT NOT NULL," +
                "CONSTRAINT fk_users_cache_user_id " +
                "FOREIGN KEY (user_id)" +
                "    REFERENCES users(id)" +
                "    ON DELETE CASCADE" +
                "    ON UPDATE CASCADE," +
                "CONSTRAINT unique_user " +
                "UNIQUE (username, discriminator)," +
                "CONSTRAINT discriminator_range " +
                "CHECK (discriminator >= 0 AND discriminator <= 9999)" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void add(@NotNull Connection con, int userId, @NotNull UserNameTag userNameTag) throws SQLException {
        String query = "INSERT INTO users_cache (user_id, username, discriminator) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            ps.setString(2, userNameTag.userName());
            ps.setShort(3, userNameTag.id());
            ps.execute();
        }
    }

    private void reset() throws SQLException {
        String query = "TRUNCATE TABLE users_cache";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
            createTableIfNotExists();
        }
    }

    @Nullable
    public UserNameTag getUserNameTag(@NotNull Connection con, long userDiscordId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock(
                "SELECT username, discriminator FROM users_cache WHERE user_id = (SELECT id FROM users WHERE discord_id = ?)");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new UserNameTag(rs.getString("username"), rs.getShort("discriminator"));
                } else {
                    return null;
                }
            }
        }
    }
}
