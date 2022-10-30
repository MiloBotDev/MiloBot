package database.dao;

import database.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class UnoDao {

    private static final Logger logger = LoggerFactory.getLogger(UnoDao.class);
    public static UnoDao instance = null;

    private UnoDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table uno ", e);
        }
    }

    private void createTableIfNotExists() throws SQLException {
        String query = "CREATE TABLE IF NOT EXISTS uno (" +
                "id INT AUTO_INCREMENT PRIMARY KEY," +
                "user_id INT NOT NULL UNIQUE," +
                "streak INT NOT NULL," +
                "total_games INT NOT NULL," +
                "total_wins INT NOT NULL," +
                "FOREIGN KEY (user_id)" +
                "    REFERENCES users(id)" +
                "    ON DELETE CASCADE" +
                "    ON UPDATE CASCADE" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        }
    }

    public static synchronized UnoDao getInstance() {
        if(instance == null) {
            instance = new UnoDao();
        }
        return instance;
    }
}
