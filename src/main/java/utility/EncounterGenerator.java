package utility;

import models.dnd.Encounter;
import models.dnd.Monster;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Generate a d&d encounter based on the average party level, the party size, the difficulty and the optional environment.
 * This class is a singleton.
 */
public class EncounterGenerator {

	private final static Logger logger = LoggerFactory.getLogger(EncounterGenerator.class);

	public static EncounterGenerator instance;
	private final Map<String, Monster> cachedMonsters;
	private ArrayList<String[]> monsters;

	private EncounterGenerator() {
		cachedMonsters = new HashMap<>();
		loadMonsters();
	}

	public static EncounterGenerator getInstance() {
		if (instance == null) {
			instance = new EncounterGenerator();
		}
		return instance;
	}

	/**
	 * Loads all the monsters from the csv file into a list.
	 */
	public void loadMonsters() {
		this.monsters = new ArrayList<>();
		ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		InputStream is = classloader.getResourceAsStream(Config.getInstance().getMonstersCsvPath());
		InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
		try (BufferedReader br = new BufferedReader(streamReader)) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] split = line.split(",");
				monsters.add(split);
			}
			logger.info("monsters.csv loaded in.");
		} catch (IOException e) {
			logger.error(e.getMessage());
			logger.info("monsters.csv file not found.");
		}
	}

	/**
	 * Calculates the xp threshold for a given party.
	 * The difficulty is in range 1-4 for easy, medium, difficult, and deadly.
	 */
	public int calculateXp(int partyLevel, int partySize, int difficulty) {
		int[][] thresholds = {
				{25, 50, 75, 100},
				{50, 100, 150, 200},
				{75, 150, 225, 400},
				{125, 250, 375, 500},
				{250, 500, 750, 1100},
				{300, 600, 900, 1400},
				{350, 750, 1100, 1700},
				{450, 900, 1400, 2100},
				{550, 1100, 1600, 2400},
				{600, 1200, 1900, 2800},
				{800, 1600, 2400, 3600},
				{1000, 2000, 3000, 4500},
				{1100, 2200, 3400, 5100},
				{1250, 2500, 3800, 5700},
				{1400, 2800, 4300, 6400},
				{1600, 3200, 4800, 7200},
				{2000, 3900, 5900, 8800},
				{2100, 4200, 6300, 9500},
				{2400, 4900, 7300, 10900},
				{2800, 5700, 8500, 12700}
		};
		int xp = thresholds[partyLevel - 1][difficulty - 1];
		xp = xp * partySize;
		return xp;
	}

	/**
	 * Generates the list of monsters in the encounter.
	 */
	public @NotNull ArrayList<Monster> generateEncounteredMonsters(String environment, int xp) {
		ArrayList<String[]> possibleMonsters = new ArrayList<>();
		if (!(environment == null)) {
			for (String[] monster : monsters) {
				if (Objects.equals(monster[1], environment)) {
					possibleMonsters.add(monster);
				}
			}
		} else {
			possibleMonsters = monsters;
		}
		int xpMonsters = 0;
		int xpLowerLimit = xp / 25;
		ArrayList<String[]> encounteredMonsters = new ArrayList<>();
		while (xpMonsters <= (xp - (3 * xpLowerLimit))) {
			ArrayList<String[]> candidates = new ArrayList<>();
			for (String[] monster : possibleMonsters) {
				if (xpLowerLimit <= (Integer.parseInt(monster[4])) && (Integer.parseInt(monster[4])) <= (xp - xpMonsters)) {
					candidates.add(monster);
				}
			}
			if (candidates.size() == 0) {
				return new ArrayList<>();
			}
			int r = new Random().nextInt(candidates.size());
			encounteredMonsters.add(candidates.get(r));
			int monsterCounter = encounteredMonsters.size();
			xpMonsters = 0;
			for (String[] exp : encounteredMonsters) {
				xpMonsters += Integer.parseInt(exp[4]);
			}
			if (monsterCounter == 2) {
				xpMonsters = (int) (xpMonsters * 1.5);
			}
			if (3 <= monsterCounter && monsterCounter <= 6) {
				xpMonsters = xpMonsters * 2;
			}
			if (7 <= monsterCounter && monsterCounter <= 10) {
				xpMonsters = (int) (xpMonsters * 2.5);
			}
		}
		return monstersToMonster(encounteredMonsters);
	}

	/**
	 * Converts the list of monsters to a list of Monster objects.
	 */
	private @NotNull ArrayList<Monster> monstersToMonster(@NotNull ArrayList<String[]> encounteredMonsters) {
		ArrayList<Monster> monsters = new ArrayList<>();
		for (String[] monster : encounteredMonsters) {
			// checking if this monster is somewhere in the cache
			if (cachedMonsters.containsKey(monster[0])) {
				monsters.add(cachedMonsters.get(monster[0]));
			} else {
				monsters.add(new Monster(monster[0], monster[1], monster[2], monster[3], monster[4]));
				// adding the monster to the cache
				cachedMonsters.put(monster[0], monsters.get(monsters.size() - 1));
			}
		}
		return monsters;
	}

	/**
	 * Generates a complete encounter.
	 **/
	public Encounter generateEncounter(int partySize, int partyLevel, String difficulty, String environment) {
		int xp = calculateXp(partyLevel, partySize, Encounter.difficultyToInt(difficulty));
		ArrayList<Monster> monsters = generateEncounteredMonsters(environment, xp);
		return new Encounter(partySize, partyLevel, difficulty, xp, monsters, environment);
	}
}
