package database.queries;

public interface CommandTrackerTableQueries {
	String checkIfCommandUsageUserTracked = "SELECT * FROM command_tracker WHERE commandName = ? AND userId = ?;";
	String addCommandUsageUserToTracker = "INSERT INTO command_tracker(commandName, userId, amount) VALUES(?, ?, ?);";
	String checkCommandUsageUserAmount = "SELECT amount FROM command_tracker WHERE commandName = ? AND userId = ?";
	String updateCommandUsageUserAmount = "UPDATE command_tracker SET amount = ? WHERE commandName = ? AND userId = ?";
	String checkCommandUsageGlobalAmount = "SELECT sum(CAST(amount as INT)) from command_tracker WHERE commandName = ?;";
}
