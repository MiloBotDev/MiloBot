package io.github.milobotdev.milobot.api.session;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Represents the keys used for signing and encrypting JWTs in java security RSA key format.
 *
 * @param signaturePublicKey The key used to verify the signature of a JWT.
 * @param signaturePrivateKey The key used to sign a JWT.
 * @param encryptionPublicKey The key used to encrypt a JWT.
 * @param encryptionPrivateKey The key used to decrypt a JWT.
 */
public record JWTRSAKeys(RSAPublicKey signaturePublicKey, RSAPrivateKey signaturePrivateKey, RSAPublicKey encryptionPublicKey,
                         RSAPrivateKey encryptionPrivateKey) {
}
