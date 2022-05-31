package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsible for creating the connection to the database.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Connect {

    /**
     * The url of the database to connect to.
     */
    private static final String CONNECTION_URL = "jdbc:sqlite:C:/sqlite/IdleAway.db";

    /**
     * Attempts to make a connection to the database.
     * @return the Connection object.
     */
    public static Connection connectToDb() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(CONNECTION_URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }
}
