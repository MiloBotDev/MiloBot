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
 */
@Deprecated(forRemoval = true, since = "8/12/22")
public class DatabaseManager {

	final static Logger logger = LoggerFactory.getLogger(DatabaseManager.class);

	private static DatabaseManager instance;

	private final String connectionUrl;
	private final String user;
	private final String password;

	private final SQLiteConfig sqliteConfig;


	/**
	 * A private constructor since this class is a singleton.
	 */
	private DatabaseManager() {
		this.sqliteConfig = new SQLiteConfig();
		this.sqliteConfig.resetOpenMode(SQLiteOpenMode.CREATE);
		this.sqliteConfig.setPragma(SQLiteConfig.Pragma.FOREIGN_KEYS, "ON");
		Config config = Config.getInstance();
		this.connectionUrl = config.getConnectionUrl();
		this.user = config.getUser();
		this.password = config.getPassword();
	}

	/**
	 * Returns the DatabaseManager and makes an instance of it if needed.
	 */
	public static DatabaseManager getInstance() {
		if (instance == null) {
			instance = new DatabaseManager();
		}
		return instance;
	}

	/**
	 * Makes a connection to the database.
	 */
	public Connection connect() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(this.connectionUrl, this.user, this.password);
			conn.setClientInfo(sqliteConfig.toProperties());
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return conn;
	}

	/**
	 * Send a query to the database.
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
					logger.info(String.format("Executed query: %s", stmt));
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
			// TODO: handle exception
			System.out.println(e.getMessage());
		}
		return result;
	}

	/**
	 * The type of query you want to send.
	 * UPDATE has no return value.
	 * RETURN has a return value.
	 */
	public enum QueryTypes {UPDATE, RETURN}
}
