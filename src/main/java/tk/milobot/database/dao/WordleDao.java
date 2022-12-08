package tk.milobot.database.dao;

import tk.milobot.database.model.Wordle;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class WordleDao {

    private static final Logger logger = LoggerFactory.getLogger(UserDao.class);
    private static WordleDao instance = null;

    private WordleDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table wordle ", e);
        }
    }

    public static synchronized WordleDao getInstance() {
        if (instance == null) {
            instance = new WordleDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        UserDao.getInstance();
        String query = "CREATE TABLE IF NOT EXISTS wordle (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
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
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void add(@NotNull Connection con, @NotNull Wordle wordle) throws SQLException {
        String query = "INSERT INTO wordle (user_id, games_played, wins, fastest_time, highest_streak, current_streak) VALUES (?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, wordle.getUserId());
        ps.setInt(2, wordle.getGamesPlayed());
        ps.setInt(3, wordle.getWins());
        ps.setInt(4, wordle.getFastestTime());
        ps.setInt(5, wordle.getHighestStreak());
        ps.setInt(6, wordle.getCurrentStreak());
        ps.execute();
    }

    public void update(@NotNull Connection con, @NotNull Wordle wordle) throws SQLException {
        String query = "UPDATE wordle SET games_played = ?, wins = ?, fastest_time = ?, highest_streak = ?, current_streak = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, wordle.getGamesPlayed());
        ps.setInt(2, wordle.getWins());
        ps.setInt(3, wordle.getFastestTime());
        ps.setInt(4, wordle.getHighestStreak());
        ps.setInt(5, wordle.getCurrentStreak());
        ps.setInt(6, wordle.getId());
        ps.execute();
    }

    public List<Wordle> getTopHighestStreak() throws SQLException {
        ArrayList<Wordle> highestStreaks = new ArrayList<>();
        String query = "SELECT * FROM wordle ORDER BY wordle.highest_streak DESC LIMIT 100;";
        return getWordles(highestStreaks, query);
    }

    public List<Wordle> getTopFastestTime() throws SQLException {
        ArrayList<Wordle> fastestTimes = new ArrayList<>();
        String query = "SELECT * FROM wordle ORDER BY wordle.fastest_time ASC LIMIT 100;";
        return getWordles(fastestTimes, query);
    }

    public List<Wordle> getTopTotalWins() throws SQLException {
        ArrayList<Wordle> totalWins = new ArrayList<>();
        String query = "SELECT * FROM wordle ORDER BY wordle.wins DESC LIMIT 100;";
        return getWordles(totalWins, query);
    }

    public List<Wordle> getTopTotalGames() throws SQLException {
        ArrayList<Wordle> totalGames = new ArrayList<>();
        String query = "SELECT * FROM wordle ORDER BY wordle.games_played DESC LIMIT 100;";
        return getWordles(totalGames, query);
    }

    public List<Wordle> getTopCurrentStreak() throws SQLException {
        ArrayList<Wordle> currentStreaks = new ArrayList<>();
        String query = "SELECT * FROM wordle ORDER BY wordle.current_streak DESC LIMIT 100;";
        return getWordles(currentStreaks, query);
    }

    private List<Wordle> getWordles(ArrayList<Wordle> currentStreaks, String query) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                currentStreaks.add(new Wordle(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("games_played"), rs.getInt("wins"),
                        rs.getInt("fastest_time"), rs.getInt("highest_streak"), rs.getInt("current_streak")));
            }
            return currentStreaks;
        }
    }

    @Nullable
    public Wordle getByUserId(@NotNull Connection con, int userId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM wordle WHERE user_id = ?");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Wordle(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("games_played"), rs.getInt("wins"),
                            rs.getInt("fastest_time"), rs.getInt("highest_streak"), rs.getInt("current_streak"));
                }
            }
            return null;
        }
    }

    @Nullable
    public Wordle getByUserDiscordId(@NotNull Connection con, long userDiscordId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM wordle WHERE user_id = (SELECT id FROM users WHERE discord_id = ?)");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Wordle(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("games_played"), rs.getInt("wins"),
                            rs.getInt("fastest_time"), rs.getInt("highest_streak"), rs.getInt("current_streak"));
                }
            }
            return null;
        }
    }
}
