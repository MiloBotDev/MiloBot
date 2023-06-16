package io.github.milobotdev.milobot.api.session;

public record JWTKeys(byte[] signaturePublicKey, byte[] signaturePrivateKey, byte[] encryptionPublicKey,
            byte[] encryptionPrivateKey) {
}