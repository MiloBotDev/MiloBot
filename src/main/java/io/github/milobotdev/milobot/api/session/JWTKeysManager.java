package io.github.milobotdev.milobot.api.session;

import io.github.milobotdev.milobot.database.dao.JWTKeysDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class JWTKeysManager {
    private static final Logger logger = LoggerFactory.getLogger(JWTKeysManager.class);

    private static JWTKeysManager instance = null;
    private final JWTKeysDao dao = JWTKeysDao.getInstance();
    private final JWTKeys keys;
    private final JWTRSAKeys rsaKeys;

    private JWTKeysManager() {
        keys = dao.getKeys();
        KeyFactory kf = null; // or "EC" or whatever
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

    public JWTKeys getKeys() {
        return keys;
    }

    public JWTRSAKeys getRSAKeys() {
        return rsaKeys;
    }
}
