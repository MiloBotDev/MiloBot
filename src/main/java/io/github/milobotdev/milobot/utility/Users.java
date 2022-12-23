package io.github.milobotdev.milobot.utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.milobotdev.milobot.database.dao.DailyDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.UsersCacheDao;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.games.hungergames.HungerGames;
import io.github.milobotdev.milobot.models.UserNameTag;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

/**
 * All methods related to users.
 * This class is a singleton.
 */
public class Users {

    final static Logger logger = LoggerFactory.getLogger(Users.class);

    private static Users instance;
    public final HashMap<Integer, Integer> levels;
    private final UserDao userDao;
    private final DailyDao dailyDao;
    private final UsersCacheDao usersCacheDao;
    public int maxLevel;

    private Users() {
        this.userDao = UserDao.getInstance();
        this.dailyDao = DailyDao.getInstance();
        this.usersCacheDao = UsersCacheDao.getInstance();
        Config config = Config.getInstance();
        this.levels = new HashMap<>();
        loadLevelsAsMap();
    }

    /**
     * Get the only existing instance of this class.
     *
     * @return The instance of this class.
     */
    public static synchronized Users getInstance() {
        if (instance == null) {
            instance = new Users();
        }
        return instance;
    }

    /**
     * Adds a user to the database if it does not already exist.
     *
     * @param userDiscordId discord id of the user
     */
    public void addUserIfNotExists(long userDiscordId) {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            if (userDao.getUserByDiscordId(con, userDiscordId, RowLockType.FOR_UPDATE) == null) {
                userDao.add(con, new io.github.milobotdev.milobot.database.model.User(userDiscordId));
            }
            con.commit();
        } catch (SQLException e) {
            logger.error("Error adding user to database", e);
        }
    }

    /**
     * Updates a users experience and increases their level if needed.
     *
     * @param discordUserId - The id of the user
     * @param experience    - The amount of experience to add
     */
    public void updateExperience(long discordUserId, int experience, String asMention, MessageChannel channel) throws SQLException {
        // load in their current experience and level
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            io.github.milobotdev.milobot.database.model.User user = userDao.getUserByDiscordId(con, discordUserId, RowLockType.FOR_UPDATE);
            Objects.requireNonNull(user).addExperience(experience);
            // check if they leveled up
            if (user.getLevel() < maxLevel) {
                int nextLevel = user.getLevel() + 1;
                int nextLevelExperience = levels.get(nextLevel);
                if (user.getExperience() >= nextLevelExperience) {
                    // user leveled up so update their level and experience
                    user.incrementLevel();
                    userDao.update(con, user);
                    logger.trace(String.format("%s leveled up to level %d!", discordUserId, nextLevel));
                    // send a message to the channel the user leveled up in
                    channel.sendMessage(String.format("%s leveled up to level %d!", asMention, nextLevel)).queue();
                } else {
                    userDao.update(con, user);
                }
            } else {
                userDao.update(con, user);
            }
            con.commit();
        }
    }

    /**
     * Loads the levels.json file into a HashMap.
     */
    private void loadLevelsAsMap() {
        // see https://stackoverflow.com/a/48298758
        try {
            URI uri = getClass().getResource("/levels.json").toURI();
            HungerGames.fileLoadHack(uri);
            Path source = Paths.get(uri);
            String jsonAsString = new String(Files.readAllBytes(source));
            JsonArray asJsonArray = JsonParser.parseString(jsonAsString).getAsJsonArray();
            for (int i = 0; i < asJsonArray.size(); i++) {
                JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
                int level = asJsonObject.get("level").getAsInt();
                int experience = asJsonObject.get("experience").getAsInt();
                levels.put(level, experience);
                if (i + 1 == asJsonArray.size()) {
                    // the last level in the json is the max level
                    maxLevel = level;
                }
            }
            logger.debug("Levels.json loaded in.");
        } catch (IOException | URISyntaxException e) {
            logger.error("Unable to load levels.json.", e);
        }
    }

    public UserNameTag getUserNameTag(long discordId, JDA jda) {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            UserNameTag userNameTag = usersCacheDao.getUserNameTag(con, discordId, RowLockType.FOR_UPDATE);
            if (userNameTag == null) {
                User user = jda.retrieveUserById(discordId).complete();
                userNameTag = new UserNameTag(user.getName(), Short.parseShort(user.getDiscriminator()));
                usersCacheDao.add(con, Objects.requireNonNull(userDao.getUserByDiscordId(con, discordId, RowLockType.FOR_UPDATE)).getId(), userNameTag);
            }
            con.commit();
            return userNameTag;
        } catch (SQLException e) {
            logger.error("Error getting user name tag", e);
        }
        return null;
    }
}
