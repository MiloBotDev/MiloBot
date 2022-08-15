package database;

import database.queries.WordleTableQueries;
import newdb.dao.UserDao;
import newdb.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class DatabaseSeeder {
	private static void fillDatabase() {
		DatabaseManager manager = DatabaseManager.getInstance();
		final UserDao userDao = UserDao.getInstance();
		final Logger logger = LoggerFactory.getLogger(DatabaseSeeder.class);
		Random random = new Random();
		for (int i = 0; i < 100; i++) {
			long userId = ThreadLocalRandom.current().nextLong(Long.MAX_VALUE);
			int currency = random.nextInt(1000);
			int level = random.nextInt(100);
			int experience = random.nextInt(10000);
			String fastestTime = String.valueOf(random.nextInt(300));
			String streak = String.valueOf(random.nextInt(10));
			String totalGames = String.valueOf(random.nextInt(100));
			String highestStreak = String.valueOf(random.nextInt(7));
			String wonLastGame = "true";

			User user = new User(userId, currency, level, experience);
			try {
				userDao.add(user);
			} catch (SQLException e) {
				logger.error("DatabaseSeeder: Error adding user to database", e);
				continue;
			}
			manager.query(WordleTableQueries.addUserWordle, DatabaseManager.QueryTypes.UPDATE, String.valueOf(userId),
					fastestTime, wonLastGame, streak, totalGames, highestStreak);
		}
	}

	public static void main(String[] args) {
		fillDatabase();
	}
}
