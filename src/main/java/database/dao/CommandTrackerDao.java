package database.dao;

import database.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class CommandTrackerDao {

    private static final Connection con = DatabaseConnection.getConnection();
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static CommandTrackerDao instance = null;

    private CommandTrackerDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table command_tracker ", e);
        }
    }

    public static CommandTrackerDao getInstance() {
        if (instance == null) {
            instance = new CommandTrackerDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS command_tracker (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL," +
                "command_name VARCHAR(25) NOT NULL," +
                "amount INT NOT NULL," +
                "CONSTRAINT fk_user_id FOREIGN KEY (user_id) " +
                "REFERENCES users(id) " +
                "ON DELETE CASCADE " +
                "ON UPDATE CASCADE" +
                ");";
        Statement st = con.createStatement();
        st.execute(query);
    }

    public boolean checkIfUserCommandTracked(String commandName, int userId) throws SQLException {
        String query = "SELECT * FROM command_tracker WHERE command_name = ? AND user_id = ?;";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, commandName);
        ps.setLong(2, userId);
        ResultSet rs = ps.executeQuery();
        return rs.next();
    }

    public void addUserCommandTracker(String commandName, int userId) throws SQLException {
        String query = "INSERT INTO command_tracker(command_name, user_id, amount) VALUES(?, ?, ?);";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, commandName);
        ps.setInt(2, userId);
        ps.setInt(3, 1);
        ps.executeUpdate();
    }

    public void updateUserCommandTracker(String commandName, int userId, int amount) throws SQLException {
        String query = "UPDATE command_tracker SET amount = ? WHERE command_name = ? AND user_id = ?;";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, amount);
        ps.setString(2, commandName);
        ps.setInt(3, userId);
        ps.executeUpdate();
    }

    public int getUserSpecificCommandUsage(String commandName, int userId) throws SQLException {
        String query = "SELECT amount FROM command_tracker WHERE command_name = ? AND user_id = ?;";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, commandName);
        ps.setInt(2, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt("amount");
        } else {
            return 0;
        }
    }

    public int getGlobalCommandUsage(String commandName) throws SQLException {
        String query = "SELECT sum(amount) from command_tracker WHERE command_name = ?;";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, commandName);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return rs.getInt(1);
        } else {
            return 0;
        }
    }
}
