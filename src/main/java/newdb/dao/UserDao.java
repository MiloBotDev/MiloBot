package newdb.dao;

import newdb.model.User;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public interface UserDao {
    void add(User user) throws SQLException;
    void delete(User user) throws SQLException;
    void update(User user) throws SQLException;
    @Nullable
    User getUserByDiscordId(long discordId) throws SQLException;
}
