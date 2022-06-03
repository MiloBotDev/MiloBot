package database.queries;

import database.Connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class CreateTables {

    private final static String commandUsageTable = "CREATE TABLE IF NOT EXISTS command_usage (CommandName varchar(255), amount int);";

    private static void createTables() {
        Connection conn;
        Statement stmt;
        try {
            conn = Connect.connectToDb();
            stmt = conn.createStatement();
            stmt.executeUpdate(commandUsageTable);
            stmt.close();
            conn.close();
            System.out.println("Tables successfully created!");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        createTables();
    }
}
