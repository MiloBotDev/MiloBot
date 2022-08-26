package utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import database.DatabaseManager;
import games.HungerGames;
import net.dv8tion.jda.api.entities.MessageChannel;
import newdb.dao.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

/**
 * All methods related to users.
 * This class is a singleton.
 */
public class User {

    final static Logger logger = LoggerFactory.getLogger(User.class);

    private static User instance;
    public final HashMap<Integer, Integer> levels;
    private final DatabaseManager manager;
    private final UserDao userDao;
    public int maxLevel;

    private User() {
        this.manager = DatabaseManager.getInstance();
        this.userDao = UserDao.getInstance();
        Config config = Config.getInstance();
        String levelsJsonPath = config.getLevelsJsonPath();
        this.levels = new HashMap<>();
        loadLevelsAsMap();
    }

    /**
     * Get the only existing instance of this class.
     *
     * @return The instance of this class.
     */
    public static User getInstance() {
        if (instance == null) {
            instance = new User();
        }
        return instance;
    }

    /**
     * Checks if a user exists in the database.
     *
     * @param userDiscordId - The id of the user
     * @return true if the user exists, false if not
     */
    public boolean checkIfUserExists(long userDiscordId) {
        boolean exists = false;
        try {
            if (userDao.getUserByDiscordId(userDiscordId) != null) {
                exists = true;
            }
        } catch (SQLException e) {
            logger.error("Error checking if user exists", e);
        }
        return exists;
    }

    /**
     * Updates a users experience and increases their level if needed.
     *
     * @param discordUserId - The id of the user
     * @param experience    - The amount of experience to add
     */
    public void updateExperience(long discordUserId, int experience, String asMention, MessageChannel channel) throws SQLException {
        // load in their current experience and level
        newdb.model.User user = userDao.getUserByDiscordId(discordUserId);
        Objects.requireNonNull(user).addExperience(experience);
        // check if they leveled up
        if (user.getLevel() < maxLevel) {
            int nextLevel = user.getLevel() + 1;
            int nextLevelExperience = levels.get(nextLevel);
            if (user.getExperience() >= nextLevelExperience) {
                // user leveled up so update their level and experience
                user.incrementLevel();
                userDao.update(user);
                logger.info(String.format("%s leveled up to level %d!", discordUserId, nextLevel));
                // send a message to the channel the user leveled up in
                channel.sendMessage(String.format("%s leveled up to level %d!", asMention, nextLevel)).queue();
            } else {
                userDao.update(user);
            }
        } else {
            userDao.update(user);
        }
    }

    /**
     * Loads the levels.json file into a HashMap.
     */
    private void loadLevelsAsMap() {
        // see https://stackoverflow.com/a/48298758
        try {
            URI uri = getClass().getResource(Config.getInstance().getLevelsJsonPath()).toURI();
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
            logger.info("Levels.json loaded in.");
        } catch (IOException | URISyntaxException e) {
            logger.error(e.getMessage());
            logger.info("Unable to load levels.json.");
        }
    }
}
