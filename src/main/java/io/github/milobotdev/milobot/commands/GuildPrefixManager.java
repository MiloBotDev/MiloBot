package io.github.milobotdev.milobot.commands;

import io.github.milobotdev.milobot.database.dao.PrefixDao;
import io.github.milobotdev.milobot.database.model.Prefix;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.main.JDAManager;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import io.github.milobotdev.milobot.utility.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class is responsible for managing the prefixes of the guilds.
 * This class is a singleton.
 */
public class GuildPrefixManager {

    private static GuildPrefixManager instance;
    private final PrefixDao prefixDao = PrefixDao.getInstance();
    public final ConcurrentHashMap<Long, String> prefixes = new ConcurrentHashMap<>();

    private GuildPrefixManager() {}

    /**
     * Returns an instance of the guild prefix manager.
     *
     * @return an instance of the guild prefix manager.
     */
    public static synchronized GuildPrefixManager getInstance() {
        if (instance == null) {
            instance = new GuildPrefixManager();
        }
        return instance;
    }

    /**
     * Initializes the guild prefix manager.
     */
    public void initialize() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(new ListenerAdapter() {
            @Override
            public void onGuildLeave(@NotNull GuildLeaveEvent event) {
                setPrefix(event.getGuild().getIdLong(), null);
            }
        });
    }

    public EventListener getEventListener() {
        return new ListenerAdapter() {
            @Override
            public void onGuildLeave(@NotNull GuildLeaveEvent event) {
                setPrefix(event.getGuild().getIdLong(), null);
            }
        };
    }

    /**
     * Sets the prefix of the guild.
     *
     * @param guildId the id of the guild.
     * @param prefix the prefix of the guild.
     */
    public void setPrefix(long guildId, String prefix) {
        prefixes.compute(guildId, (id, oldPrefix) -> {
            try (Connection con = DatabaseConnection.getConnection()) {
                if (prefix == null) {
                    prefixDao.deleteByGuildId(con, guildId);
                } else {
                    con.setAutoCommit(false);
                    Prefix prefixDbObj = prefixDao.getPrefixByGuildId(con, guildId, RowLockType.FOR_UPDATE);
                    if (prefixDbObj != null) {
                        prefixDbObj.setPrefix(prefix);
                        prefixDao.update(con, prefixDbObj);
                    } else {
                        prefixDao.add(con, new Prefix(guildId, prefix));
                    }
                    con.commit();
                }
            } catch (SQLException e) {
                throw new RuntimeException("Could not update prefix for guild", e);
            }
            return prefix;
        });
    }

    /**
     * Returns the prefix of the guild.
     *
     * @param guildId the id of the guild.
     * @return the prefix of the guild.
     */
    public String getPrefix(long guildId) {
        return prefixes.computeIfAbsent(guildId, this::getGuildPrefixFromDb);
    }

    /**
     * Returns the prefix of the guild from the database. This method is marked as private because
     * it is intended to be used only in this class. Other classes should use the {@link #getPrefix(long)}
     * method instead, which caches guild prefixes in memory.
     *
     * @param guildId the id of the guild.
     * @return the prefix of the guild from the database.
     */
    private String getGuildPrefixFromDb(long guildId) {
        String dbPrefix;
        try (Connection con = DatabaseConnection.getConnection()) {
            Prefix prefixObj = prefixDao.getPrefixByGuildId(con, guildId, RowLockType.NONE);
            if (prefixObj == null) {
                dbPrefix = Config.getInstance().getDefaultPrefix();
            } else {
                dbPrefix = prefixObj.getPrefix();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while getting prefix from database", e);
        }

        return dbPrefix;
    }
}
