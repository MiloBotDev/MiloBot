package io.github.milobotdev.milobot.api.session;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Generates the keys used for signing and encrypting JWTs.
 */
public class JWTKeyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JWTKeyGenerator.class);

    /**
     * Generates the keys used for signing and encrypting JWTs.
     *
     * @return The keys used for signing and encrypting JWTs.
     */
    @NotNull
    public static JWTKeys generateKeys() {
        logger.debug("Generating JWT signature and encryption keys");

        byte[] signaturePublicKey;
        byte[] signaturePrivateKey;
        byte[] encryptionPublicKey;
        byte[] encryptionPrivateKey;
        try {
            RSAKey signatureJWK = new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.SIGNATURE)
                    .generate();
            signaturePublicKey = signatureJWK.toRSAPublicKey().getEncoded();
            signaturePrivateKey = signatureJWK.toRSAPrivateKey().getEncoded();

            RSAKey encryptionJWK = new RSAKeyGenerator(2048)
                    .keyUse(KeyUse.ENCRYPTION)
                    .generate();
            encryptionPublicKey = encryptionJWK.toRSAPublicKey().getEncoded();
            encryptionPrivateKey = encryptionJWK.toRSAPrivateKey().getEncoded();
        } catch (JOSEException e) {
            logger.error("Error generating JWT signature and encryption keys", e);
            throw new RuntimeException(e);
        }
        return new JWTKeys(signaturePublicKey, signaturePrivateKey, encryptionPublicKey, encryptionPrivateKey);
    }
}
