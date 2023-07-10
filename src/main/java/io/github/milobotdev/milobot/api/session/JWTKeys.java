package io.github.milobotdev.milobot.api.session;

/**
 * Represents the keys used for signing and encrypting JWTs in byte array format.
 * Used by {@link JWTKeyGenerator} to return the signing and encryption keys.
 *
 * @param signaturePublicKey The key used to verify the signature of a JWT.
 * @param signaturePrivateKey The key used to sign a JWT.
 * @param encryptionPublicKey The key used to encrypt a JWT.
 * @param encryptionPrivateKey The key used to decrypt a JWT.
 */
public record JWTKeys(byte[] signaturePublicKey, byte[] signaturePrivateKey, byte[] encryptionPublicKey,
            byte[] encryptionPrivateKey) {
}