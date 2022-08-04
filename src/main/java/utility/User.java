package utility;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import database.DatabaseManager;
import database.queries.UsersTableQueries;
import games.HungerGames;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * All methods related to users.
 * This class is a singleton.
 */
public class User {

	final static Logger logger = LoggerFactory.getLogger(User.class);

	private static User instance;
	public final HashMap<Integer, Integer> levels;
	private final DatabaseManager manager;
	public int maxLevel;

	private User() {
		this.manager = DatabaseManager.getInstance();
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
	 * @param userId - The id of the user
	 * @return true if the user exists, false if not
	 */
	public boolean checkIfUserExists(String userId) {
		boolean exists = false;
		ArrayList<String> result = manager.query(UsersTableQueries.selectUser, DatabaseManager.QueryTypes.RETURN, userId);
		if (result.size() > 0) {
			exists = true;
		}
		return exists;
	}

	/**
	 * Updates a users experience and increases their level if needed.
	 *
	 * @param userId     - The id of the user
	 * @param experience - The amount of experience to add
	 */
	public void updateExperience(String userId, int experience, String asMention, MessageChannel channel) {
		// load in their current experience and level
		ArrayList<String> query = manager.query(UsersTableQueries.getUserExperienceAndLevel, DatabaseManager.QueryTypes.RETURN, userId);
		int currentExperience = Integer.parseInt(query.get(0));
		int currentLevel = Integer.parseInt(query.get(1));
		int newExperience = currentExperience + experience;
		// check if they leveled up
		if (currentLevel < maxLevel) {
			int nextLevel = currentLevel + 1;
			int nextLevelExperience = levels.get(nextLevel);
			if (newExperience >= nextLevelExperience) {
				// user leveled up so update their level and experience
				manager.query(UsersTableQueries.updateUserLevelAndExperience, DatabaseManager.QueryTypes.UPDATE, String.valueOf(nextLevel),
						String.valueOf(newExperience), userId);
				logger.info(String.format("%s leveled up to level %d!", userId, nextLevel));
				// send a message to the channel the user leveled up in
				channel.sendMessage(String.format("%s leveled up to level %d!", asMention, nextLevel)).queue();
			} else {
				// user didn't level up so just update their experience
				manager.query(UsersTableQueries.updateUserExperience, DatabaseManager.QueryTypes.UPDATE, String.valueOf(newExperience),
						userId);
			}
		} else {
			// user didn't level up so just update their experience
			manager.query(UsersTableQueries.updateUserExperience, DatabaseManager.QueryTypes.UPDATE, String.valueOf(newExperience),
					userId);
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
