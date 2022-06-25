package user;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import database.DatabaseManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

/**
 * All functions related to users.
 * This class is a singleton.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class User {

    final static Logger logger = LoggerFactory.getLogger(User.class);

    private static User instance;
    private final DatabaseManager manager;
    private final String levelsJsonPath;
    public int maxLevel;

    public final HashMap<Integer, Integer> levels;

    private User() {
        this.manager = DatabaseManager.getInstance();
        Config config = Config.getInstance();
        this.levelsJsonPath = config.levelsJsonPath;
        this.levels = new HashMap<>();
        loadLevelsAsMap();
    }

    /**
     * Get the instance of this class.
     * @return The instance of this class.
     */
    public static User getInstance() {
        if(instance == null) {
            instance = new User();
        }
        return instance;
    }

    /**
     * Checks if a user exists in the database.
     * @param userId - The id of the user
     * @return true if the user exists, false if not
     */
    public boolean checkIfUserExists(String userId) {
        boolean exists = false;
        ArrayList<String> result = manager.query(manager.selectUser, DatabaseManager.QueryTypes.RETURN, userId);
        if (result.size() > 0) {
            exists = true;
        }
        return exists;
    }

    /**
     * Updates a users experience and increases their level if needed.
     * @param userId - The id of the user
     * @param experience - The amount of experience to add
     */
    public void updateExperience(String userId, int experience, MessageReceivedEvent event) {
        // load in their current experience and level
        ArrayList<String> query = manager.query(manager.getUserExperienceAndLevel, DatabaseManager.QueryTypes.RETURN, userId);
        int currentExperience = Integer.parseInt(query.get(0));
        int currentLevel = Integer.parseInt(query.get(1));
        int newExperience = currentExperience + experience;
        // check if they leveled up
        if(currentLevel < maxLevel) {
            int nextLevel = currentLevel + 1;
            int nextLevelExperience = levels.get(nextLevel);
            if (newExperience >= nextLevelExperience) {
                // user leveled up so update their level and experience
                manager.query(manager.updateUserLevelAndExperience, DatabaseManager.QueryTypes.UPDATE, String.valueOf(nextLevel),
                        String.valueOf(newExperience), userId);
                logger.info(String.format("%s leveled up to level %d!", userId, nextLevel));
                // send a message to the channel the user leveled up in
                String asMention = event.getAuthor().getAsMention();
                event.getChannel().sendMessage(String.format("%s leveled up to level %d!", asMention, nextLevel)).queue();
            } else {
                // user didn't level up so just update their experience
                manager.query(manager.updateUserExperience, DatabaseManager.QueryTypes.UPDATE, String.valueOf(newExperience),
                        userId);
            }
        } else {
            // user didn't level up so just update their experience
            manager.query(manager.updateUserExperience, DatabaseManager.QueryTypes.UPDATE, String.valueOf(newExperience),
                    userId);
        }
    }

    /**
     * Loads the levels.json file into a HashMap.
     */
    private void loadLevelsAsMap() {
        try {
            String jsonAsString = new String(Files.readAllBytes(Paths.get(levelsJsonPath)));
            JsonArray asJsonArray = JsonParser.parseString(jsonAsString).getAsJsonArray();
            for (int i = 0; i < asJsonArray.size(); i++) {
                JsonObject asJsonObject = asJsonArray.get(i).getAsJsonObject();
                int level = asJsonObject.get("level").getAsInt();
                int experience = asJsonObject.get("experience").getAsInt();
                levels.put(level, experience);
                if(i+1 == asJsonArray.size()) {
                    // the last level in the json is the max level
                    maxLevel = level;
                }
            }
            logger.info("Levels.json loaded in.");
        } catch (IOException e) {
            logger.error(e.getMessage());
            logger.info("Unable to load levels.json.");
        }
    }
}
