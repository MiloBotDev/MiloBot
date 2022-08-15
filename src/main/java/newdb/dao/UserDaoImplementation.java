package newdb.dao;

import newdb.model.User;
import newdb.util.DatabaseConnection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class UserDaoImplementation implements UserDao {
    private static final Connection con = DatabaseConnection.getConnection();
    private static UserDaoImplementation instance = null;
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImplementation.class);

    private UserDaoImplementation() {
        try {
            creteTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table users ", e);
        }
    }

    public static UserDao getInstance() {
        if (instance == null) {
            instance = new UserDaoImplementation();
        }
        return instance;
    }

    private void creteTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "discord_id BIGINT NOT NULL UNIQUE," +
                "currency INT NOT NULL," +
                "level INT NOT NULL," +
                "experience INT NOT NULL" +
                ")";
        Statement st = con.createStatement();
        st.execute(query);
    }

    @Override
    public void add(User user) throws SQLException {
        String query = "INSERT INTO users (discord_id, currency, level, experience) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, user.getDiscordId());
        ps.setInt(2, user.getCurrency());
        ps.setInt(3, user.getLevel());
        ps.setInt(4, user.getExperience());
        ps.executeUpdate();
    }

    @Override
    public void delete(User user) throws SQLException {
        String query = "DELETE FROM users WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, user.getDiscordId());
        ps.executeUpdate();
    }

    @Override
    public void update(User user) throws SQLException {
        String query = "UPDATE users SET discord_id = ?, currency = ?, level = ?, experience = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, user.getDiscordId());
        ps.setInt(2, user.getCurrency());
        ps.setInt(3, user.getLevel());
        ps.setInt(4, user.getExperience());
        ps.setInt(5, user.getId());
        ps.executeUpdate();
    }

    @Override
    @Nullable
    public User getUserByDiscordId(long discordId) throws SQLException {
        String query = "SELECT * FROM users WHERE discord_id = ?";
        PreparedStatement ps;
        ps = con.prepareStatement(query);
        ps.setLong(1, discordId);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            return new User(rs.getInt("id"), rs.getLong("discord_id"), rs.getInt("currency"),
                    rs.getInt("level"), rs.getInt("experience"));
        } else {
            return null;
        }
    }

    @Override
    public int getUserRank(int userId) throws SQLException {
        String query = "SELECT RANK() OVER (ORDER BY experience DESC) `rank` FROM users WHERE id = ?";
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
}
