package io.github.milobotdev.milobot.database.dao;

import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import io.github.milobotdev.milobot.api.session.JWTKeyGenerator;
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
                JWTKeyGenerator.Keys keys = JWTKeyGenerator.generateKeys();

                query = "INSERT INTO jwt_keys (signature_public_key, signature_private_key, encryption_public_key, encryption_private_key) VALUES (?, ?, ?, ?)";
                try (java.sql.PreparedStatement ps = con.prepareStatement(query)) {
                    ps.setBlob(1, new ByteArrayInputStream(keys.signaturePublicKey()));
                    ps.setBlob(2, new ByteArrayInputStream(keys.signaturePrivateKey()));
                    ps.setBlob(3, new ByteArrayInputStream(keys.encryptionPublicKey()));
                    ps.setBlob(4, new ByteArrayInputStream(keys.encryptionPrivateKey()));
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
}
