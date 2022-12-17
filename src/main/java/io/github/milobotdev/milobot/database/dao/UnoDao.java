package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.model.Uno;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Optional;

public class UnoDao {

    private static final Logger logger = LoggerFactory.getLogger(UnoDao.class);
    public static UnoDao instance = null;

    private UnoDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table uno ", e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS uno (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "streak INT NOT NULL," +
                "highest_streak INT NOT NULL," +
                "total_games_played INT NOT NULL," +
                "total_wins INT NOT NULL," +
                "total_cards_played INT NOT NULL," +
                "total_cards_drawn INT NOT NULL," +
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

    public void add(@NotNull Connection con, @NotNull Uno uno) throws SQLException {
        String query = "INSERT INTO uno (user_id, streak, highest_streak, total_games_played, total_wins, total_cards_played, total_cards_drawn) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, uno.getUserId());
            ps.setInt(2, uno.getStreak());
            ps.setInt(3, uno.getHigheststreak());
            ps.setInt(4, uno.getTotalGamesPlayed());
            ps.setInt(5, uno.getTotalWins());
            ps.setInt(6, uno.getTotalCardsPlayed());
            ps.setInt(7, uno.getTotalCardsDrawn());
            ps.executeUpdate();
        }
    }

    public void update(@NotNull Connection con, @NotNull Uno uno) throws SQLException {
        String query = "UPDATE uno SET streak = ?, highest_streak = ?, total_games_played = ?, total_wins = ?, total_cards_played = ?, total_cards_drawn = ? WHERE user_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setInt(1, uno.getStreak());
            ps.setInt(2, uno.getHigheststreak());
            ps.setInt(3, uno.getTotalGamesPlayed());
            ps.setInt(4, uno.getTotalWins());
            ps.setInt(5, uno.getTotalCardsPlayed());
            ps.setInt(6, uno.getTotalCardsDrawn());
            ps.setLong(7, uno.getUserId());
            ps.executeUpdate();
        }
    }

    public Optional<Uno> getByUserDiscordId(@NotNull Connection con, long userDiscordId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock(
                "SELECT * FROM uno INNER JOIN users ON users.id = uno.user_id WHERE users.discord_id = ?");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, userDiscordId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Uno(rs.getInt("id"), rs.getInt("user_id"),
                            rs.getInt("streak"), rs.getInt("highest_streak"),
                            rs.getInt("total_games_played"), rs.getInt("total_wins"),
                            rs.getInt("total_cards_played"), rs.getInt("total_cards_drawn")));
                }
                return Optional.empty();
            }
        }
    }

    public static synchronized UnoDao getInstance() {
        if (instance == null) {
            instance = new UnoDao();
        }
        return instance;
    }
}
