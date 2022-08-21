package newdb.dao;

import newdb.model.Wordle;
import newdb.util.DatabaseConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class WordleDao {

    private static final Connection con = DatabaseConnection.getConnection();
    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static WordleDao instance = null;

    private WordleDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table wordle ", e);
        }
    }

    public static WordleDao getInstance() {
        if (instance == null) {
            instance = new WordleDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS wordle (" +
                "user_id INT NOT NULL," +
                "games_played INT NOT NULL," +
                "wins INT NOT NULL," +
                "fastest_time INT NOT NULL," +
                "highest_streak INT NOT NULL," +
                "current_streak INT NOT NULL," +
                "CONSTRAINT FK_user_id_wordle FOREIGN KEY (user_id) " +
                "REFERENCES users(id) " +
                "ON UPDATE CASCADE " +
                "ON DELETE CASCADE" +
                ");";
        Statement st = con.createStatement();
        st.execute(query);
    }

    public void addUserWordle(int userId, int fastestTime, int wins, int highestStreak, int currentStreak) throws SQLException {
        String query = "INSERT INTO wordle(user_id, games_played, wins, fastest_time, highest_streak, current_streak) " +
                "VALUES(?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        ps.setInt(2, 1);
        ps.setInt(3, wins);
        ps.setInt(4, fastestTime);
        ps.setInt(5, highestStreak);
        ps.setInt(6, currentStreak);
        ps.executeUpdate();
    }

    public void updateUserWordle(int userId, int timeTaken, int wins, int highestStreak, int currentStreak, int gamesPlayed) throws SQLException {
        String query = "UPDATE wordle SET games_played = ?, wins = ?, fastest_time = ?, highest_streak = ?, current_streak = ? WHERE user_id = ?;";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, gamesPlayed);
        ps.setInt(2, wins);
        ps.setInt(3, timeTaken);
        ps.setInt(4, highestStreak);
        ps.setInt(5, currentStreak);
        ps.setInt(6, userId);
        ps.executeUpdate();
    }

    @Nullable
    public Wordle getByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM wordle WHERE user_id = ?;";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, userId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Wordle(rs.getInt("user_id"), rs.getInt("games_played"), rs.getInt("wins"),
                    rs.getInt("fastest_time"), rs.getInt("highest_streak"), rs.getInt("current_streak"));
        }
        return null;
    }
}
