package database.queries;

import database.Connect;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class DeleteDatabase implements Query {

    @Override
    public void execute() {
        Connection conn;
        Statement stmt;
        try {
            conn = Connect.connectToDb();

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        new DeleteDatabase().execute();
    }
}
