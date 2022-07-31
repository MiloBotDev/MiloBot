package database.queries;

public interface BlackjackTableQueries {
	String checkIfUserExists = "select * from blackjack where userId = ?;";
	String addUser = "insert into blackjack(userId) values(?);";
	String getUser = "select * from blackjack where userId = ?;";
	String updateUserWin =
			"update blackjack " +
			"set wonLastGame = 'true', streak = ?, totalGames = ?, totalWins = ?, totalEarnings = ?, highestStreak = ? " +
			"where userId = ?;";
	String updateUserDraw =
			"update blackjack " +
			"set wonLastGame = 'false', streak = '0', totalGames = ?, totalDraws = ?, totalEarnings = ? " +
			"where userId = ?;";
	String updateUserLoss =
			"update blackjack " +
			"set wonLastGame = 'false', streak = '0', totalGames = ?, totalEarnings = ? " +
			"where userId = ?;";

}
