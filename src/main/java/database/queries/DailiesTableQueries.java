package database.queries;

public interface DailiesTableQueries {
    String addUserDaily = "INSERT INTO dailies(userId) VALUES(?);";
    String getUserDaily = "SELECT * FROM dailies WHERE userId = ?;";
    String updateUserDaily = "UPDATE dailies SET lastDailyDate = ?, streak = ?, totalClaimed = ? WHERE userId = ?;";
}
