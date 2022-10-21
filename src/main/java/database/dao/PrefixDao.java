package database.dao;

import database.model.Prefix;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class PrefixDao {

    private static final Logger logger = LoggerFactory.getLogger(PrefixDao.class);
    private static PrefixDao instance = null;

    private PrefixDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table prefixes ", e);
        }
    }

    public static synchronized PrefixDao getInstance() {
        if (instance == null) {
            instance = new PrefixDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS prefixes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "guild_id BIGINT NOT NULL UNIQUE," +
                "prefix VARCHAR(2) NOT NULL" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public void add(@NotNull Connection con, @NotNull Prefix prefix) throws SQLException {
        String query = "INSERT INTO prefixes (guild_id, prefix) VALUES (?, ?)";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, prefix.getGuildId());
            ps.setString(2, prefix.getPrefix());
            ps.executeUpdate();
        }
    }

    public void update(@NotNull Connection con, @NotNull Prefix prefix) throws SQLException {
        String query = "UPDATE prefixes SET prefix = ? WHERE id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setString(1, prefix.getPrefix());
            ps.setInt(2, prefix.getId());
            ps.executeUpdate();
        }
    }

    public void deleteByGuildId(@NotNull Connection con, long guildId) throws SQLException {
        String query = "DELETE FROM prefixes WHERE guild_id = ?";
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, guildId);
            ps.executeUpdate();
        }
    }

    public @Nullable Prefix getPrefixByGuildId(@NotNull Connection con, long guildId, @NotNull RowLockType lockType) throws SQLException {
        String query = lockType.getQueryWithLock("SELECT * FROM prefixes WHERE guild_id = ?");
        try (PreparedStatement ps = con.prepareStatement(query)) {
            ps.setLong(1, guildId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Prefix(rs.getInt("id"),
                            rs.getLong("guild_id"),
                            rs.getString("prefix"));
                }
            }
            return null;
        }
    }
}
