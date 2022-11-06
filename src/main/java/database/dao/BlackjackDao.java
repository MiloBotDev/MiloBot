package database.dao;

import database.model.Blackjack;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlackjackDao {

    private static final Logger logger = LoggerFactory.getLogger(BlackjackDao.class);
    private static BlackjackDao instance = null;

    private BlackjackDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table prefixes ", e);
        }
    }

    public static synchronized BlackjackDao getInstance() {
        if (instance == null) {
            instance = new BlackjackDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        UserDao.getInstance();
        String query = "CREATE TABLE IF NOT EXISTS blackjack (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "won_last_game BOOLEAN NOT NULL," +
                "streak INT NOT NULL," +
                "total_games INT NOT NULL," +
                "total_wins INT NOT NULL," +
                "total_draws INT NOT NULL," +
                "total_earnings INT NOT NULL," +
                "highest_streak INT NOT NULL," +
                "CONSTRAINT fk_blackjack_user_id " +
                "FOREIGN KEY (user_id)" +
                "    REFERENCES users(id)" +
                "    ON DELETE CASCADE" +
                "    ON UPDATE CASCADE" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void add(@NotNull Connection con, @NotNull Blackjack blackjack) throws SQLException {
        String query = "INSERT INTO blackjack (user_id, won_last_game, streak, total_games, total_wins, total_draws, " +
                "total_earnings, highest_streak) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, blackjack.getUserId());
            ps.setBoolean(2, blackjack.wonLastGame());
            ps.setInt(3, blackjack.getStreak());
            ps.setInt(4, blackjack.getTotalGames());
            ps.setInt(5, blackjack.getTotalWins());
            ps.setInt(6, blackjack.getTotalDraws());
            ps.setInt(7, blackjack.getTotalEarnings());
            ps.setInt(8, blackjack.getHighestStreak());
            ps.executeUpdate();
        }
    }

    public void update(@NotNull Connection con, @NotNull Blackjack blackjack) throws SQLException {
        String query = "UPDATE blackjack SET user_id = ?, won_last_game = ?, streak = ?, total_games = ?, " +
                "total_wins = ?, total_draws = ?, total_earnings = ?, highest_streak = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, blackjack.getUserId());
            ps.setBoolean(2, blackjack.wonLastGame());
            ps.setInt(3, blackjack.getStreak());
            ps.setInt(4, blackjack.getTotalGames());
            ps.setInt(5, blackjack.getTotalWins());
            ps.setInt(6, blackjack.getTotalDraws());
            ps.setInt(7, blackjack.getTotalEarnings());
            ps.setInt(8, blackjack.getHighestStreak());
            ps.setInt(9, blackjack.getId());
            ps.executeUpdate();
        }
    }

    public List<Blackjack> getTopTotalGamesPlayed() throws SQLException {
        String query = "SELECT * FROM blackjack ORDER BY total_games DESC LIMIT 100";
        return getBlackjacks(query);
    }

    public List<Blackjack> getTopTotalWins() throws SQLException {
        String query = "SELECT * FROM blackjack ORDER BY total_wins DESC LIMIT 100";
        return getBlackjacks(query);
    }

    public List<Blackjack> getTopTotalDraws() throws SQLException {
        String query = "SELECT * FROM blackjack ORDER BY total_draws DESC LIMIT 100";
        return getBlackjacks(query);
    }

    public List<Blackjack> getTopTotalEarnings() throws SQLException {
        String query = "SELECT * FROM blackjack ORDER BY total_earnings DESC LIMIT 100";
        return getBlackjacks(query);
    }

    public List<Blackjack> getTopHighestStreak() throws SQLException {
        String query = "SELECT * FROM blackjack ORDER BY highest_streak DESC LIMIT 100";
        return getBlackjacks(query);
    }

    public List<Blackjack> getTopCurrentStreak() throws SQLException {
        String query = "SELECT * FROM blackjack ORDER BY streak DESC LIMIT 100";
        return getBlackjacks(query);
    }

    private @NotNull List<Blackjack> getBlackjacks(String query) throws SQLException {
        ArrayList<Blackjack> blackjacks = new ArrayList<>();
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                blackjacks.add(new Blackjack(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getBoolean("won_last_game"),
                        rs.getInt("streak"),
                        rs.getInt("total_games"),
                        rs.getInt("total_wins"),
                        rs.getInt("total_draws"),
                        rs.getInt("total_earnings"),
                        rs.getInt("highest_streak")
                ));
            }
        }
        return blackjacks;
    }

    @Nullable
    public Blackjack getByUserDiscordId(@NotNull Connection con, long userDiscordId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock(
                "SELECT * FROM blackjack INNER JOIN users ON blackjack.user_id = users.id WHERE users.discord_id = ?");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Blackjack(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getBoolean("won_last_game"),
                            rs.getInt("streak"), rs.getInt("total_games"),
                            rs.getInt("total_wins"), rs.getInt("total_draws"),
                            rs.getInt("total_earnings"), rs.getInt("highest_streak"));
                }
                return null;
            }
        }
    }
}
