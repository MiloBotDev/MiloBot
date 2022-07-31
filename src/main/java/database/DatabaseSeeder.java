package database;

import database.queries.UsersTableQueries;
import database.queries.WordleTableQueries;

import java.util.Locale;
import java.util.Random;
import java.util.UUID;

public class DatabaseSeeder {

	private static void fillDatabase() {
		DatabaseManager manager = DatabaseManager.getInstance();
		Random random = new Random();
		String randomName = "example";
		for (int i = 0; i < 100; i++) {
			String userId = UUID.randomUUID().toString().replaceAll("-", "").toLowerCase(Locale.ROOT);
			String currency = String.valueOf(random.nextInt(1000));
			String name = String.format("%s%d", randomName, i);
			String level = String.valueOf(random.nextInt(100));
			String experience = String.valueOf(random.nextInt(10000));
			String fastestTime = String.valueOf(random.nextInt(300));
			String streak = String.valueOf(random.nextInt(10));
			String totalGames = String.valueOf(random.nextInt(100));
			String highestStreak = String.valueOf(random.nextInt(7));
			String wonLastGame = "true";
			manager.query(UsersTableQueries.addUser, DatabaseManager.QueryTypes.UPDATE, userId, name, currency, level, experience);
			manager.query(WordleTableQueries.addUserWordle, DatabaseManager.QueryTypes.UPDATE, userId, fastestTime, wonLastGame,
					streak, totalGames, highestStreak);
		}
	}

	public static void main(String[] args) {
		fillDatabase();
	}
}
