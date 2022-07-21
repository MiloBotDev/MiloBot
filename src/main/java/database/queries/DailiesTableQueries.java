package database.queries;

public interface DailiesTableQueries {
    String createDailiesTable = "CREATE TABLE IF NOT EXISTS dailies" +
            "(" +
            "    userId        varchar(255) " +
            "        constraint dailies_user_userId_fk " +
            "            references user " +
            "            on update cascade on delete cascade, " +
            "    lastDailyDate varchar(255) default 'null', " +
            "    streak        varchar(255) default '0', " +
            "    totalClaimed varchar(255) default '0' " +
            ");";
    String addUserDaily = "INSERT INTO dailies(userId) VALUES(?);";
    String getUserDaily = "SELECT * FROM dailies WHERE userId = ?;";
    String updateUserDaily = "UPDATE dailies SET lastDailyDate = ?, streak = ?, totalClaimed = ? WHERE userId = ?;";
}
