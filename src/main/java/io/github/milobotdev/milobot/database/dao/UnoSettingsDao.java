package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.database.model.UnoSettings;
import io.github.milobotdev.milobot.database.model.User;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class UnoSettingsDao {

    private static final Logger logger = LoggerFactory.getLogger(UnoSettingsDao.class);
    private static UnoSettingsDao instance = null;

    public static synchronized UnoSettingsDao getInstance() {
        if (instance == null) {
            instance = new UnoSettingsDao();
        }
        return instance;
    }

    private UnoSettingsDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e){
            logger.error("Error creating table UnoSettings ", e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS uno_settings (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "discord_id BIGINT NOT NULL UNIQUE," +
                "turn_time_limit INT NOT NULL," +
                "starting_cards_amount INT NOT NULL" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void add(@NotNull Connection con, @NotNull UnoSettings unoSettings) throws SQLException {
        String query = "INSERT INTO uno_settings (discord_id, turn_time_limit, starting_cards_amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, unoSettings.getDiscordId());
            ps.setInt(2, unoSettings.getTurnTimeLimit());
            ps.setInt(3, unoSettings.getStartingCardsAmount());
            ps.executeUpdate();
        }
    }

    public void update(@NotNull Connection con, @NotNull UnoSettings unoSettings) throws SQLException {
        String query = "UPDATE uno_settings SET turn_time_limit = ?, starting_cards_amount = ? WHERE discord_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)){
            ps.setInt(1, unoSettings.getTurnTimeLimit());
            ps.setInt(2, unoSettings.getStartingCardsAmount());
            ps.setLong(3, unoSettings.getDiscordId());
            ps.executeUpdate();
        }
    }

    @Nullable
    public UnoSettings getByDiscordId(Connection con, RowLockType rowLockType) throws SQLException {
        return null;
    }


}
