package database.queries;

public interface WordleTableQueries {
	String createWordleTable = "CREATE TABLE IF NOT EXISTS wordle (userId varchar(255), fastestTime varchar(255), wonLastGame varchar(255), streak varchar(255), totalGames varchar(255), highestStreak varchar(255));";
	String addUserWordle = "INSERT INTO wordle(userId, fastestTime, wonLastGame, streak, totalGames, highestStreak) VALUES(?, ?, ?, ?, ?, ?)";
	String updateUserWordle = "UPDATE wordle SET fastestTime = ?, wonLastGame = ?, streak = ?, totalGames = ?, highestStreak = ? WHERE userId = ?";
	String selectUserWordle = "SELECT * FROM wordle WHERE userId = ?";
	String wordleGetTopTotalGamesPlayed = "SELECT user.name, wordle.totalGames FROM user JOIN wordle ON user.userId = wordle.userId ORDER BY CAST(wordle.totalGames AS int) DESC LIMIT 100;";
	String wordleGetTopHighestStreak = "SELECT user.name, wordle.highestStreak FROM user JOIN wordle ON user.userId = wordle.userId ORDER BY CAST(wordle.highestStreak AS int) DESC LIMIT 100;";
	String wordleGetTopCurrentStreak = "SELECT user.name, wordle.streak FROM user JOIN wordle ON user.userId = wordle.userId ORDER BY CAST(wordle.streak AS int) DESC LIMIT 100;";
}
