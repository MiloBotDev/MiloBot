package models;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class Encounter {

	private final int partySize;
	private final int partyLevel;
	private final String difficulty;
	private final String environment;
	private final List<Monster> monsters;
	private final int xpThreshold;

	public Encounter(int partySize, int partyLevel, String difficulty, int xpThreshold, List<Monster> monsters, String environment) {
		this.partySize = partySize;
		this.partyLevel = partyLevel;
		this.difficulty = difficulty;
		this.xpThreshold = xpThreshold;
		this.monsters = monsters;
		this.environment = environment;
	}

	public static int difficultyToInt(@NotNull String difficulty) {
		switch (difficulty) {
			case "easy":
				return 1;
			case "medium":
				return 2;
			case "difficult":
				return 3;
			case "deadly":
				return 4;
		}
		return 0;
	}

	@Override
	public String toString() {
		StringBuilder enc = new StringBuilder();
		for (Monster monster : monsters) {
			enc.append(String.format("**%s**, type: %s, xp value of: %s (MM pg. %s) \n",
					monster.getName(), monster.getType(), monster.getXp(), monster.getMmPage()));
		}
		enc.append(String.format("XP threshold is: %dxp", xpThreshold));
		return enc.toString();
	}


	public int getPartySize() {
		return partySize;
	}

	public int getPartyLevel() {
		return partyLevel;
	}

	public String getDifficulty() {
		return difficulty;
	}

	public String getEnvironment() {
		return environment;
	}

	public List<Monster> getMonsters() {
		return monsters;
	}

	public int getXpThreshold() {
		return xpThreshold;
	}
}
