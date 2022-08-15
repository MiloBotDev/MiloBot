package newdb.dao;

import newdb.model.Prefix;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

public interface PrefixDao {
    void add(Prefix prefix) throws SQLException;
    void update(Prefix prefix) throws SQLException;
    void deleteByGuildId(long guildId) throws SQLException;
    @Nullable
    Prefix getPrefixByGuildId(long guildId) throws SQLException;
    List<Prefix> getAllPrefixes() throws SQLException;
}
