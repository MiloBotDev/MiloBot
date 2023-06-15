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

        JWTKeyGenerator.Keys keys = JWTKeyGenerator.generateKeys();

        query = "INSERT INTO jwt_keys (signature_public_key, signature_private_key, encryption_public_key, encryption_private_key) VALUES (?, ?, ?, ?)";
        try (Connection con = DatabaseConnection.getConnection();
             java.sql.PreparedStatement ps = con.prepareStatement(query)) {
            ps.setBlob(1, new ByteArrayInputStream(keys.signaturePublicKey()));
            ps.setBlob(2, new ByteArrayInputStream(keys.signaturePrivateKey()));
            ps.setBlob(3, new ByteArrayInputStream(keys.encryptionPublicKey()));
            ps.setBlob(4, new ByteArrayInputStream(keys.encryptionPrivateKey()));
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.error("Error inserting keys into jwt_keys ", e);
        }
    }
}
