package database.queries;

/**
 * All queries on the user table.
 */
public interface UsersTableQueries {
	String removeUser = "DELETE FROM users WHERE userId = ?;";
	String selectUser = "SELECT * FROM users WHERE userId = ?;";
	String addUser = "INSERT INTO users(userId, name, currency, level, experience) VALUES(?, ?, ?, ?, ?);";
	String updateUserExperience = "UPDATE users SET experience = ? WHERE userId = ?;";
	String updateUserLevelAndExperience = "UPDATE users SET level = ?, experience = ? WHERE userId = ?;";
	String getUserExperienceAndLevel = "SELECT experience, level FROM users WHERE userId = ?;";
	String getUserRankByExperience = "SELECT rank FROM (SELECT userId, row_number() over () as rank FROM (SELECT userId FROM (SELECT userId FROM users ORDER BY CAST(experience AS INT) DESC) AS table3) AS table2) AS table1 WHERE userId = ?";
	String getUserAmount = "SELECT count(*) FROM users;";
	String updateUserName = "UPDATE users SET name = ? WHERE userId = ?;";
	String getAllUserIdsAndNames = "SELECT userId, name FROM users";
	String getUserCurrency = "SELECT currency FROM users WHERE userId = ?;";
	String updateUserCurrency = "UPDATE users SET currency = ? WHERE userId = ?;";
}
