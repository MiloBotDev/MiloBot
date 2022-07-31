package database.queries;

public interface PrefixTableQueries {
	String addServerPrefix = "INSERT INTO prefix(serverId, prefix) VALUES(?, ?);";
	String deleteServerPrefix = "DELETE FROM prefix WHERE serverId = ?;";
	String updateServerPrefix = "UPDATE prefix SET prefix = ? WHERE serverId = ?;";
	String getAllPrefixes = "SELECT serverId, prefix FROM prefix;";
}
