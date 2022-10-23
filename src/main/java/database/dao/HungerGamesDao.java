package database.dao;

import database.model.HungerGames;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HungerGamesDao {

    private static final Logger logger = LoggerFactory.getLogger(HungerGamesDao.class);
    private static HungerGamesDao instance = null;

    private HungerGamesDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table hungergames ", e);
        }
    }

    public static synchronized HungerGamesDao getInstance() {
        if (instance == null) {
            instance = new HungerGamesDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        UserDao.getInstance();
        String query = "CREATE TABLE IF NOT EXISTS hungergames (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "total_kills INT NOT NULL," +
                "total_damage_done INT NOT NULL," +
                "total_damage_taken INT NOT NULL," +
                "total_healing_done INT NOT NULL," +
                "total_items_collected INT NOT NULL," +
                "total_games_played INT NOT NULL," +
                "total_wins INT NOT NULL," +
                "CONSTRAINT fk_hungergames_user_id " +
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

    public void add(@NotNull Connection con, @NotNull HungerGames hungerGames) throws SQLException {
        String query = "INSERT INTO hungergames (user_id, total_kills, total_damage_done, total_damage_taken, total_healing_done, " +
                "total_items_collected, total_games_played, total_wins) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, hungerGames.getUserId());
        ps.setInt(2, hungerGames.getTotalKills());
        ps.setInt(3, hungerGames.getTotalDamageDone());
        ps.setInt(4, hungerGames.getTotalDamageTaken());
        ps.setInt(5, hungerGames.getTotalHealingDone());
        ps.setInt(6, hungerGames.getTotalItemsCollected());
        ps.setInt(7, hungerGames.getTotalGamesPlayed());
        ps.setInt(8, hungerGames.getTotalWins());
        ps.execute();
    }

    public void update(@NotNull Connection con, @NotNull HungerGames hungerGames) throws SQLException {
        String query = "UPDATE hungergames SET total_kills = ?, total_damage_done = ?, total_damage_taken = ?, " +
                "total_healing_done = ?, total_items_collected = ?, total_games_played = ?, total_wins = ? WHERE user_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, hungerGames.getTotalKills());
        ps.setInt(2, hungerGames.getTotalDamageDone());
        ps.setInt(3, hungerGames.getTotalDamageTaken());
        ps.setInt(4, hungerGames.getTotalHealingDone());
        ps.setInt(5, hungerGames.getTotalItemsCollected());
        ps.setInt(6, hungerGames.getTotalGamesPlayed());
        ps.setInt(7, hungerGames.getTotalWins());
        ps.setLong(8, hungerGames.getUserId());
        ps.execute();
    }

    public List<HungerGames> getTopTotalKills() throws SQLException {
        ArrayList<HungerGames> highestKills = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_kills DESC LIMIT 100;";
        return getHungerGames(highestKills, query);
    }

    public List<HungerGames> getTopTotalDamageDone() throws SQLException {
        ArrayList<HungerGames> highestDamageDone = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_damage_done DESC LIMIT 100;";
        return getHungerGames(highestDamageDone, query);
    }

    public List<HungerGames> getTopTotalDamageTaken() throws SQLException {
        ArrayList<HungerGames> highestDamageTaken = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_damage_taken DESC LIMIT 100;";
        return getHungerGames(highestDamageTaken, query);
    }

    public List<HungerGames> getTopTotalHealingDone() throws SQLException {
        ArrayList<HungerGames> highestHealingDone = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_healing_done DESC LIMIT 100;";
        return getHungerGames(highestHealingDone, query);
    }

    public List<HungerGames> getTopTotalItemsCollected() throws SQLException {
        ArrayList<HungerGames> highestItemsCollected = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_items_collected DESC LIMIT 100;";
        return getHungerGames(highestItemsCollected, query);
    }

    public List<HungerGames> getTopTotalGamesPlayed() throws SQLException {
        ArrayList<HungerGames> highestGamesPlayed = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_games_played DESC LIMIT 100;";
        return getHungerGames(highestGamesPlayed, query);
    }

    public List<HungerGames> getTopTotalWins() throws SQLException {
        ArrayList<HungerGames> highestWins = new ArrayList<>();
        String query = "SELECT * FROM hungergames ORDER BY hungergames.total_wins DESC LIMIT 100;";
        return getHungerGames(highestWins, query);
    }

    private List<HungerGames> getHungerGames(ArrayList<HungerGames> hgList, String query) throws SQLException {
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.prepareStatement(query);
             ResultSet rs = st.executeQuery(query)) {
            while (rs.next()) {
                HungerGames hungerGames = new HungerGames(rs.getInt("id"), rs.getInt("user_id"), rs.getInt("total_kills"),
                        rs.getInt("total_damage_done"), rs.getInt("total_damage_taken"), rs.getInt("total_healing_done"),
                        rs.getInt("total_items_collected"), rs.getInt("total_games_played"), rs.getInt("total_wins"));
                hgList.add(hungerGames);
            }
            return hgList;
        }
    }

    @Nullable
    public HungerGames getByUserDiscordId(@NotNull Connection con, long userDiscordId, @NotNull RowLockType locktype) throws SQLException {
        String query = locktype.getQueryWithLock("SELECT * FROM hungergames WHERE user_id = (SELECT id FROM users WHERE discord_id = ?)");
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, userDiscordId);
        var rs = ps.executeQuery();
        if (rs.next()) {
            return new HungerGames(
                    rs.getInt("id"),
                    rs.getInt("user_id"),
                    rs.getInt("total_kills"),
                    rs.getInt("total_damage_done"),
                    rs.getInt("total_damage_taken"),
                    rs.getInt("total_healing_done"),
                    rs.getInt("total_items_collected"),
                    rs.getInt("total_games_played"),
                    rs.getInt("total_wins")
            );
        }
        return null;
    }
}
