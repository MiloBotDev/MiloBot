package io.github.milobotdev.milobot.database.util;

import org.apache.commons.dbcp2.BasicDataSource;
import io.github.milobotdev.milobot.utility.Config;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnection {

    private static DataSource ds;

    static {
        Config config = Config.getInstance();
        String url = config.getConnectionUrl();
        String user = config.getUser();
        String pass = config.getPassword();

        setupDataSource(url, user, pass);
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    private static void setupDataSource(String connectURI, String user, String password) {
        BasicDataSource ds = new BasicDataSource();
        ds.setDriverClassName("com.mysql.cj.jdbc.Driver");
        ds.setUrl(connectURI);
        ds.setUsername(user);
        ds.setPassword(password);
        DatabaseConnection.ds = ds;
    }

    private static void printDataSourceStats() {
        BasicDataSource bds = (BasicDataSource) ds;
        System.out.println("NumActive: " + bds.getNumActive());
        System.out.println("NumIdle: " + bds.getNumIdle());
    }

    private static void shutdownDataSource() throws SQLException {
        BasicDataSource bds = (BasicDataSource) ds;
        bds.close();
    }
}
