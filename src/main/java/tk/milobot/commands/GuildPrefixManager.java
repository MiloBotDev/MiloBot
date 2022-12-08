package tk.milobot.commands;

import tk.milobot.database.dao.PrefixDao;
import tk.milobot.database.model.Prefix;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import tk.milobot.main.JDAManager;
import tk.milobot.utility.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

public class GuildPrefixManager {
    private static GuildPrefixManager instance;
    private final PrefixDao prefixDao = PrefixDao.getInstance();
    public final ConcurrentHashMap<Long, String> prefixes = new ConcurrentHashMap<>();

    public static synchronized GuildPrefixManager getInstance() {
        if (instance == null) {
            instance = new GuildPrefixManager();
        }
        return instance;
    }

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

    public String getPrefix(long guildId) {
        return prefixes.computeIfAbsent(guildId, this::getGuildPrefixFromDb);
    }

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