package database.util;

import utility.Config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

@Deprecated(since="10/2/22, Migrating toward Apache DBCP in NewDatabaseConnection class", forRemoval=true)
public class DatabaseConnection {

    private static Connection con = null;

    static {
        Config config = Config.getInstance();
        String url = config.getConnectionUrl();
        String user = config.getUser();
        String pass = config.getPassword();

        try {
            con = DriverManager.getConnection(url, user, pass);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return con;
    }
}