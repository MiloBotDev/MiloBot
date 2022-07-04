package database.queries;

public interface CommandTrackerTableQueries {
	String createCommandUsageUserTable = "CREATE TABLE IF NOT EXISTS command_tracker (commandName varchar(255), userId varchar(255), amount varchar(255));";
	String checkIfCommandUsageUserTracked = "SELECT * FROM command_tracker WHERE commandName = ? AND userId = ?;";
	String addCommandUsageUserToTracker = "INSERT INTO command_tracker(commandName, userId, amount) VALUES(?, ?, ?);";
	String checkCommandUsageUserAmount = "SELECT amount FROM command_tracker WHERE commandName = ? AND userId = ?";
	String updateCommandUsageUserAmount = "UPDATE command_tracker SET amount = ? WHERE commandName = ? AND userId = ?";
}
