package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.model.CommandTracker;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public int getUserTotalCommandUsage(int userId) throws SQLException {
        String query = "SELECT sum(amount) from command_tracker WHERE user_id = ?;";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                } else {
                    return 0;
                }
            }
        }
    }

    public String getUserMostUsedCommand(int userId) throws SQLException {
        String query = "SELECT c.command_name FROM command_tracker AS ct " +
                "JOIN commands AS c ON ct.command = c.id " +
                "WHERE ct.user_id = ? " +
                "ORDER BY ct.amount DESC " +
                "LIMIT 1;";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("command_name");
                } else {
                    return null;
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

    public List<CommandTracker> getTotalCommandUsage() throws SQLException {
        List<CommandTracker> commandTrackers = new ArrayList<>();
        String query = "SELECT SUM(ct.amount) AS amount, c.command_name AS name FROM command_tracker AS ct " +
                "JOIN commands AS c ON ct.command = c.id " +
                "GROUP BY c.command_name;";
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            try (ResultSet rs = ps.executeQuery()) {
                while(rs.next()) {
                    commandTrackers.add(new CommandTracker(rs.getString("name"), rs.getInt("amount")));
                }
            }
        }
        return commandTrackers;
    }
}
