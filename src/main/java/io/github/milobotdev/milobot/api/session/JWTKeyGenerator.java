package io.github.milobotdev.milobot.api.session;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JWTKeyGenerator {
    private static final Logger logger = LoggerFactory.getLogger(JWTKeyGenerator.class);

    public record Keys(byte[] signaturePublicKey, byte[] signaturePrivateKey, byte[] encryptionPublicKey, byte[] encryptionPrivateKey) {
    }

    @NotNull
    public static Keys generateKeys() {
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
        return new Keys(signaturePublicKey, signaturePrivateKey, encryptionPublicKey, encryptionPrivateKey);
    }
}
