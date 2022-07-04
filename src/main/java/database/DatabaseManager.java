package database;

import database.queries.CommandTrackerTableQueries;
import database.queries.PrefixTableQueries;
import database.queries.UserTableQueries;
import database.queries.WordleTableQueries;
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
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class DatabaseManager {

	final static Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

	private static DatabaseManager instance;

	private final String connectionUrl;
	private final SQLiteConfig sqliteConfig;


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
	 *
	 * @return The DatabaseManger object
	 */
	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	/**
	 * Makes a connection to the database.
	 *
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
	 *
	 * @return ArrayList<String> with the result of the query, null if no result
	 */
	public ArrayList<String> query(String query, @NotNull QueryTypes types, String... args) {
		ArrayList<String> result = null;
		try {
			Connection conn = connect();
			Statement stmt;
			if (types.equals(QueryTypes.RETURN)) {
				stmt = conn.prepareStatement(query);
				for (int i = 0; i < args.length; i++) {
					((PreparedStatement) stmt).setString(i + 1, args[i]);
				}
				ResultSet results = ((PreparedStatement) stmt).executeQuery();
				logger.info(String.format("Executed query: %s", stmt));

				ResultSetMetaData rsmd = results.getMetaData();
				int columnCount = rsmd.getColumnCount();
				result = new ArrayList<>(columnCount);

				while (results.next()) {
					int i = 1;
					while (i <= columnCount) {
						result.add(results.getString(i++));
					}
				}
			} else {
				stmt = conn.createStatement();
				if (args.length == 0) {
					stmt.executeUpdate(query);
				} else {
					stmt = conn.prepareStatement(query);
					for (int i = 0; i < args.length; i++) {
						((PreparedStatement) stmt).setString(i + 1, args[i]);
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
		query(PrefixTableQueries.createPrefixTable, QueryTypes.UPDATE);
		query(CommandTrackerTableQueries.createCommandUsageUserTable, QueryTypes.UPDATE);
		query(UserTableQueries.createUserTable, QueryTypes.UPDATE);
		query(WordleTableQueries.createWordleTable, QueryTypes.UPDATE);
	}

	/**
	 * The type of query you want to send.
	 * UPDATE has no return value.
	 * RETURN has a return value.
	 */
	public enum QueryTypes {UPDATE, RETURN}
}
