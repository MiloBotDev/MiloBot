package io.github.milobotdev.milobot.database.dao;

import io.github.milobotdev.milobot.api.session.JWTKeyGenerator;
import io.github.milobotdev.milobot.api.session.JWTKeys;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JWTKeysDao {
    private static final Logger logger = LoggerFactory.getLogger(JWTKeysDao.class);
    private static JWTKeysDao instance = null;

    private JWTKeysDao() {
        try {
            createTableIfNotExists();
        } catch (SQLException e) {
            logger.error("Error creating table prefixes ", e);
        }
    }

    public static synchronized JWTKeysDao getInstance() {
        if (instance == null) {
            instance = new JWTKeysDao();
        }
        return instance;
    }

    private void createTableIfNotExists() throws SQLException {
        UserDao.getInstance();
        String query = "CREATE TABLE IF NOT EXISTS jwt_keys (" +
                "signature_public_key BLOB NOT NULL," +
                "signature_private_key BLOB NOT NULL," +
                "encryption_public_key BLOB NOT NULL," +
                "encryption_private_key BLOB NOT NULL" +
                ")";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            st.execute(query);
        } catch (SQLException e) {
            logger.error("Error creating table jwt_keys ", e);
            return;
        }

        insertKeys();
    }

    private void insertKeys() {
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            String query = "LOCK TABLES jwt_keys WRITE";
            try (Statement st = con.createStatement()) {
                st.execute(query);
            }
            boolean addKeys;
            query = "SELECT COUNT(*) FROM jwt_keys";
            try (Statement st = con.createStatement()) {
                ResultSet rs = st.executeQuery(query);
                rs.next();
                addKeys = rs.getInt(1) == 0;
            }
            if (addKeys) {
                logger.debug("No keys in jwt_keys table, adding keys");
                JWTKeys keys = JWTKeyGenerator.generateKeys();

                query = "INSERT INTO jwt_keys (signature_public_key, signature_private_key, encryption_public_key, encryption_private_key) VALUES (?, ?, ?, ?)";
                try (java.sql.PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setBytes(1, keys.signaturePublicKey());
                    ps.setBytes(2, keys.signaturePrivateKey());
                    ps.setBytes(3, keys.encryptionPublicKey());
                    ps.setBytes(4, keys.encryptionPrivateKey());
                    ps.executeUpdate();
                } catch (SQLException e) {
                    logger.error("Error inserting keys into jwt_keys ", e);
                }
            } else {
                logger.debug("Keys already in jwt_keys table, skipping");
            }
            query = "UNLOCK TABLES";
            try (Statement st = con.createStatement()) {
                st.execute(query);
            }
            con.commit();
        } catch (SQLException e) {
            logger.error("Error adding keys to jwt_keys table", e);
        }
    }

    public JWTKeys getKeys() {
        JWTKeys keys = null;
        String query = "SELECT * FROM jwt_keys";
        try (Connection con = DatabaseConnection.getConnection();
             Statement st = con.createStatement()) {
            ResultSet rs = st.executeQuery(query);
            if (rs.next()) {
                keys = new JWTKeys(
                        rs.getBytes("signature_public_key"),
                        rs.getBytes("signature_private_key"),
                        rs.getBytes("encryption_public_key"),
                        rs.getBytes("encryption_private_key")
                );
            }
        } catch (SQLException e) {
            logger.error("Error getting keys from jwt_keys table", e);
        }
        return keys;
    }
}
