package newdb.dao;

import newdb.model.Blackjack;
import newdb.util.DatabaseConnection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class BlackjackDao {
    private static final Connection con = DatabaseConnection.getConnection();
    private static final Logger logger = LoggerFactory.getLogger(BlackjackDao.class);
    private static BlackjackDao instance = null;

    private BlackjackDao() {
        try {
            creteTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table prefixes ", e);
        }
    }

    public static BlackjackDao getInstance() {
        if (instance == null) {
            instance = new BlackjackDao();
        }
        return instance;
    }

    private void creteTableIfNotExists() throws SQLException {
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
        Statement st = con.createStatement();
        st.execute(query);
    }

    public void add(Blackjack blackjack) throws SQLException {
        String query = "INSERT INTO blackjack (user_id, won_last_game, streak, total_games, total_wins, total_draws, " +
                "total_earnings, highest_streak) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
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

    public void update(Blackjack blackjack) throws SQLException {
        String query = "UPDATE blackjack SET user_id = ?, won_last_game = ?, streak = ?, total_games = ?, " +
                "total_wins = ?, total_draws = ?, total_earnings = ?, highest_streak = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
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

    @Nullable
    public Blackjack getByUserDiscordId(long userDiscordId) throws SQLException {
        String query = "SELECT * FROM blackjack INNER JOIN users ON blackjack.user_id = users.id WHERE users.discord_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, userDiscordId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Blackjack(rs.getInt("id"), rs.getInt("user_id"), rs.getBoolean("won_last_game"),
                    rs.getInt("streak"), rs.getInt("total_games"), rs.getInt("total_wins"), rs.getInt("total_draws"),
                    rs.getInt("total_earnings"), rs.getInt("highest_streak"));
        }
        return null;
    }
}
