package database;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sqlite.SQLiteConfig;
import org.sqlite.SQLiteOpenMode;
import utility.Config;

import java.sql.*;
import java.util.ArrayList;

/**
 * Manages queries sent to the database.
 * This class is a singleton.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class DatabaseManager {

    final static Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

    private static DatabaseManager instance;

    private final String connectionUrl;
    private final SQLiteConfig sqliteConfig;

    public final String createCommandUsageTable = "CREATE TABLE IF NOT EXISTS command_usage (commandName varchar(255), amount varchar(255));";
    public final String createPrefixTable = "CREATE TABLE IF NOT EXISTS prefix (serverId varchar(255), prefix varchar(255));";
    public final String createCommandUsageUserTable = "CREATE TABLE IF NOT EXISTS command_usage_user (commandName varchar(255), userId varchar(255), amount varchar(255));";
    public final String createUserTable = "CREATE TABLE IF NOT EXISTS user (userId varchar(255), name varchar(255), currency varchar(255), level varchar(255), experience varchar(255));";
    public final String createWordleTable = "CREATE TABLE IF NOT EXISTS wordle (userId varchar(255), fastestTime varchar(255), wonLastGame varchar(255), streak varchar(255), totalGames varchar(255), highestStreak varchar(255));";
    public final String getAllCommandUsages = "SELECT * FROM command_usage;";
    public final String checkIfCommandUsageUserTracked = "SELECT * FROM command_usage_user WHERE commandName = ? AND userId = ?;";
    public final String checkIfCommandTracked = "SELECT CommandName FROM command_usage WHERE commandName = ?;";
    public final String addCommandToTracker = "INSERT INTO command_usage(commandName, amount) VALUES(?, ?);";
    public final String addCommandUsageUserToTracker = "INSERT INTO command_usage_user(commandName, userId, amount) VALUES(?, ?, ?);";
    public final String checkCommandUsageAmount = "SELECT amount FROM command_usage WHERE commandName = ?";
    public final String checkCommandUsageUserAmount = "SELECT amount FROM command_usage_user WHERE commandName = ? AND userId = ?";
    public final String updateCommandUsageAmount = "UPDATE command_usage SET amount = ? WHERE commandName = ?";
    public final String updateCommandUsageUserAmount = "UPDATE command_usage_user SET amount = ? WHERE commandName = ? AND userId = ?";
    public final String addServerPrefix = "INSERT INTO prefix(serverId, prefix) VALUES(?, ?);";
    public final String deleteServerPrefix = "DELETE FROM prefix WHERE serverId = ?;";
    public final String updateServerPrefix = "UPDATE prefix SET prefix = ? WHERE serverId = ?;";
    public final String getAllPrefixes = "SELECT serverId, prefix FROM prefix;";
    public final String addUser = "INSERT INTO user(userId, name, currency, level, experience) VALUES(?, ?, ?, ?, ?);";
    public final String addUserWordle = "INSERT INTO wordle(userId, fastestTime, wonLastGame, streak, totalGames, highestStreak) VALUES(?, ?, ?, ?, ?, ?)";
    public final String updateUserWordle = "UPDATE wordle SET fastestTime = ?, wonLastGame = ?, streak = ?, totalGames = ?, highestStreak = ? WHERE userId = ?";
    public final String selectUser = "SELECT * FROM user WHERE userId = ?;";
    public final String selectUserWordle = "SELECT * FROM wordle WHERE userId = ?";
    public final String updateUserExperience = "UPDATE user SET experience = ? WHERE userId = ?;";
    public final String updateUserLevelAndExperience = "UPDATE user SET level = ?, experience = ? WHERE userId = ?;";
    public final String getUserExperienceAndLevel = "SELECT experience, level FROM user WHERE userId = ?;";
    public final String getUserRankByExperience = "SELECT rank FROM (SELECT userId, row_number() over () as rank FROM (SELECT userId FROM (SELECT userId FROM user ORDER BY experience DESC))) WHERE userId = ?";
    public final String getUserAmount = "SELECT count(*) FROM user;";

    /**
     * The type of query you want to send.
     * UPDATE has no return value.
     * RETURN has a return value.
     */
    public enum QueryTypes {UPDATE,RETURN}

    /**
     * A private constructor since this class is a singleton.
     */
    private DatabaseManager() {
        this.sqliteConfig = new SQLiteConfig();
        this.sqliteConfig.resetOpenMode(SQLiteOpenMode.CREATE);
        Config config = Config.getInstance();
        this.connectionUrl = config.connectionUrl;
    }

    /**
     * Returns the DatabaseManager and makes an instance of it if needed.
     * @return The DatabaseManger object
     */
    public static DatabaseManager getInstance() {
        if(instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    /**
     * Makes a connection to the database.
     * @return the connection as a Connection object
     */
    public Connection connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(connectionUrl, sqliteConfig.toProperties());
        } catch (SQLException e) {
            logger.info("Could not connect to the database.");
        }
        return conn;
    }

    /**
     * Send a query to the database.
     * @param query - The query as a String
     * @param types - The type of query as a QueryTypes
     * @param args - Optional arguments
     * @return ArrayList<String> with the result of the query, null if no result
     */
    public ArrayList<String> query(String query, @NotNull QueryTypes types, String... args) {
        ArrayList<String> result = null;
        try {
            Connection conn = connect();
            Statement stmt;
            if(types.equals(QueryTypes.RETURN)) {
                stmt = conn.prepareStatement(query);
                for(int i=0; i < args.length; i++) {
                    ((PreparedStatement) stmt).setString(i+1, args[i]);
                }
                ResultSet results = ((PreparedStatement) stmt).executeQuery();
                logger.info(String.format("Executed query: %s", stmt));

                ResultSetMetaData rsmd = results.getMetaData();
                int columnCount = rsmd.getColumnCount();
                result =  new ArrayList<>(columnCount);

                while (results.next()) {
                    int i = 1;
                    while(i <= columnCount) {
                        result.add(results.getString(i++));
                    }
                }
            } else {
                stmt = conn.createStatement();
                if(args.length == 0) {
                    stmt.executeUpdate(query);
                } else {
                    stmt = conn.prepareStatement(query);
                    for(int i=0; i < args.length; i++) {
                        ((PreparedStatement) stmt).setString(i+1, args[i]);
                    }
                    ((PreparedStatement) stmt).executeUpdate();
                    logger.info(String.format("Executed query: %s", stmt));
                }
            }
            stmt.close();
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    /**
     * Creates a new database.
     */
    public void createNewDatabase() {
        String url = "jdbc:sqlite:C:/sqlite/" + "IdleAway.db";
        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                logger.info("Created a new database.");
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Creates all tables.
     */
    public void createAndFillAllTables() {
        query(createCommandUsageTable, QueryTypes.UPDATE);
        query(createPrefixTable, QueryTypes.UPDATE);
        query(createCommandUsageUserTable, QueryTypes.UPDATE);
        query(createUserTable, QueryTypes.UPDATE);
        query(createWordleTable, QueryTypes.UPDATE);
    }
}
