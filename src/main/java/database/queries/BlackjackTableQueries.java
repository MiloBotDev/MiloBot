package database.queries;

public interface BlackjackTableQueries {
	String createBlackjackTable = "CREATE TABLE IF NOT EXISTS blackjack" +
			"(" +
			"    userId        varchar(255) " +
			"        constraint blackjack_user_userId_fk " +
			"            references user\n" +
			"            on update cascade on delete cascade, " +
			"    wonLastGame   varchar(10)  default '0', " +
			"    streak        varchar(255) default '0', " +
			"    totalGames    varchar(255) default '0', " +
			"    totalWins     varchar(255) default '0', " +
			"    totalEarnings varchar(255) default '0', " +
			"    totalDraws    varchar(255) default '0', " +
			"    highestStreak varchar(255) default '0' " +
			")";
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
