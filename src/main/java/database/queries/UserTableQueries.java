package database.queries;

/**
 * All queries on the user table.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public interface UserTableQueries {
	String createUserTable = "CREATE TABLE IF NOT EXISTS user" +
			"(" +
			"    userId     varchar(255) not null " +
			"        constraint user_pk " +
			"            primary key, " +
			"    name       varchar(255), " +
			"    currency   varchar(255), " +
			"    level      varchar(255), " +
			"    experience varchar(255) " +
			");" +
			"CREATE UNIQUE INDEX IF NOT EXISTS user_userId_uindex " +
			"    on user (userId);";
	String removeUser = "DELETE FROM user WHERE userId = ?;";
	String selectUser = "SELECT * FROM user WHERE userId = ?;";
	String addUser = "INSERT INTO user(userId, name, currency, level, experience) VALUES(?, ?, ?, ?, ?);";
	String updateUserExperience = "UPDATE user SET experience = ? WHERE userId = ?;";
	String updateUserLevelAndExperience = "UPDATE user SET level = ?, experience = ? WHERE userId = ?;";
	String getUserExperienceAndLevel = "SELECT experience, level FROM user WHERE userId = ?;";
	String getUserRankByExperience = "SELECT rank FROM (SELECT userId, row_number() over () as rank FROM (SELECT userId FROM (SELECT userId FROM user ORDER BY CAST(experience AS INT) DESC))) WHERE userId = ?";
	String getUserAmount = "SELECT count(*) FROM user;";
	String updateUserName = "UPDATE user SET name = ? WHERE userId = ?;";
	String getAllUserIdsAndNames = "SELECT userId, name FROM user";
	String getUserCurrency = "SELECT currency FROM user WHERE userId = ?;";
	String updateUserCurrency = "UPDATE user SET currency = ? WHERE userId = ?;";
}
