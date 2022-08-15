package newdb.dao;

import newdb.model.Prefix;
import newdb.util.DatabaseConnection;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class PrefixDaoImplementation implements PrefixDao {
    private static final Connection con = DatabaseConnection.getConnection();
    private static PrefixDaoImplementation instance = null;
    private static final Logger logger = LoggerFactory.getLogger(UserDaoImplementation.class);

    private PrefixDaoImplementation() {
        try {
            creteTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table prefixes ", e);
        }
    }

    private void creteTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS prefixes (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "guild_id BIGINT NOT NULL UNIQUE," +
                "prefix VARCHAR(2) NOT NULL" +
                ")";
        Statement st = con.createStatement();
        st.execute(query);
    }

    public static PrefixDaoImplementation getInstance() {
        if (instance == null) {
            instance = new PrefixDaoImplementation();
        }
        return instance;
    }

    @Override
    public void add(Prefix prefix) throws SQLException {
        String query = "INSERT INTO prefixes (guild_id, prefix) VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, prefix.getGuildId());
        ps.setString(2, prefix.getPrefix());
        ps.executeUpdate();
    }

    @Override
    public void update(Prefix prefix) throws SQLException {
        String query = "UPDATE prefixes SET prefix = ? WHERE id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setString(1, prefix.getPrefix());
        ps.setLong(2, prefix.getId());
        ps.executeUpdate();
    }

    @Override
    public void deleteByGuildId(long guildId) throws SQLException {
        String query = "DELETE FROM prefixes WHERE guild_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, guildId);
        ps.executeUpdate();
    }

    @Override
    public @Nullable Prefix getPrefixByGuildId(long guildId) throws SQLException {
        String query = "SELECT * FROM prefixes WHERE guild_id = ?";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setLong(1, guildId);
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Prefix(rs.getLong("guild_id"),
                    rs.getString("prefix"));
        }
        return null;
    }

    @Override
    public List<Prefix> getAllPrefixes() throws SQLException {
        String query = "SELECT * FROM prefixes";
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery(query);
        List<Prefix> prefixes = new java.util.ArrayList<>();
        while (rs.next()) {
            prefixes.add(new Prefix(rs.getInt("id"),
                    rs.getLong("guild_id"),
                    rs.getString("prefix")));
        }
        return prefixes;
    }
}
