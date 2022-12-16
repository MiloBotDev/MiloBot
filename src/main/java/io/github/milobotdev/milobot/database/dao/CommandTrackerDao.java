package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CommandTrackerDao {

    private static final Logger logger = LoggerFactory.getLogger(CommandTrackerDao.class);
    private static CommandTrackerDao instance = null;

    private CommandTrackerDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table command_tracker ", e);
        }
    }

    public static synchronized CommandTrackerDao getInstance() {
        if (instance == null) {
            instance = new CommandTrackerDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS commands (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "command_name VARCHAR(255) NOT NULL" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
        UserDao.getInstance();
        query = "CREATE TABLE IF NOT EXISTS command_tracker (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "command INT NOT NULL," +
                "amount INT NOT NULL," +
                "CONSTRAINT fk_user_id FOREIGN KEY (user_id) " +
                "REFERENCES users(id) " +
                "ON DELETE CASCADE " +
                "ON UPDATE CASCADE," +
                "CONSTRAINT fk_command FOREIGN KEY (command) " +
                "REFERENCES commands(id) " +
                "ON DELETE CASCADE " +
                "ON UPDATE CASCADE," +
                "UNIQUE KEY uc_user_command (user_id, command)" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void addToCommandTracker(@NotNull String commandName, long userDiscordId) throws SQLException {
        addCommand(commandName);
        String query = "INSERT INTO command_tracker (user_id, command, amount) VALUES " +
                "((SELECT id FROM users WHERE discord_id = ?), (SELECT id FROM commands WHERE command_name = ?), 1) " +
                "ON DUPLICATE KEY UPDATE amount = amount + 1";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            ps.setString(2, commandName);
            ps.executeUpdate();
        }
    }

    private void addCommand(String commandName) throws SQLException {
        String query = RowLockType.FOR_UPDATE.getQueryWithLock("SELECT * FROM commands WHERE command_name = ?");
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            con.setAutoCommit(false);
            ps.setString(1, commandName);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    query = "INSERT INTO commands (command_name) VALUES (?)";
                    try (PreparedStatement ps2 = con.prepareStatement(query)) {
                        ps2.setString(1, commandName);
                        ps2.executeUpdate();
                    }
                }
            }
            con.commit();
        }
    }

    public int getUserSpecificCommandUsage(String commandName, int userId) throws SQLException {
        String query = "SELECT amount FROM command_tracker WHERE command = " +
                "(SELECT id FROM commands WHERE command_name = ?) AND user_id = ?;";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, commandName);
            ps.setInt(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("amount");
                } else {
                    return 0;
                }
            }
        }
    }
    public int getGlobalCommandUsage(String commandName) throws SQLException {
        String query = "SELECT sum(amount) from command_tracker WHERE command = " +
                "(SELECT id FROM commands WHERE command_name = ?);";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, commandName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }
}
