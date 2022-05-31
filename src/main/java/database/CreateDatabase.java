package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Responsible for creating a new database.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class CreateDatabase {

    /**
     * Creates a new database with a given filename.
     * @param fileName - the name of the database as a String
     */
    public static void createNewDatabase(String fileName) {

        String url = "jdbc:sqlite:C:/sqlite/" + fileName;

        try {
            Connection conn = DriverManager.getConnection(url);
            if (conn != null) {
                System.out.println("A new database has been created.");
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    public static void main(String[] args) {
        createNewDatabase("IdleAway.db");
    }

}
