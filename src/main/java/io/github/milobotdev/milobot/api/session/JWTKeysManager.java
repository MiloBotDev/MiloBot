package io.github.milobotdev.milobot.api.session;

import io.github.milobotdev.milobot.database.dao.JWTKeysDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Manages the keys used for signing and encrypting JWTs.
 */
public class JWTKeysManager {
    private static final Logger logger = LoggerFactory.getLogger(JWTKeysManager.class);

    private static JWTKeysManager instance = null;
    private final JWTKeys keys;
    private final JWTRSAKeys rsaKeys;

    private JWTKeysManager() {
        JWTKeysDao dao = JWTKeysDao.getInstance();
        keys = dao.getKeys();
        KeyFactory kf;
        RSAPrivateKey signaturePrivateKey;
        RSAPublicKey signaturePublicKey;
        RSAPublicKey encryptionPublicKey;
        RSAPrivateKey encryptionPrivateKey;
        try {
            kf = KeyFactory.getInstance("RSA");
            signaturePrivateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(keys.signaturePrivateKey()));
            signaturePublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keys.signaturePublicKey()));
            encryptionPublicKey = (RSAPublicKey) kf.generatePublic(new X509EncodedKeySpec(keys.encryptionPublicKey()));
            encryptionPrivateKey = (RSAPrivateKey) kf.generatePrivate(new PKCS8EncodedKeySpec(keys.encryptionPrivateKey()));
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("Error creating private and public key objects for signature and encryption RSA keys.", e);
            throw new RuntimeException(e);
        }
        rsaKeys = new JWTRSAKeys(signaturePublicKey, signaturePrivateKey, encryptionPublicKey, encryptionPrivateKey);
    }

    public static synchronized JWTKeysManager getInstance() {
        if (instance == null) {
            instance = new JWTKeysManager();
        }
        return instance;
    }

    /**
     * Returns the keys used for signing and encrypting JWTs in byte array format.
     *
     * @return The keys used for signing and encrypting JWTs in byte array format.
     */
    public JWTKeys getKeys() {
        return keys;
    }

    /**
     * Returns the keys used for signing and encrypting JWTs in java security RSA key format.
     *
     * @return The keys used for signing and encrypting JWTs in java security RSA key format.
     */
    public JWTRSAKeys getRSAKeys() {
        return rsaKeys;
    }
}
