package io.github.milobotdev.milobot.api.session;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public record JWTRSAKeys(RSAPublicKey signaturePublicKey, RSAPrivateKey signaturePrivateKey, RSAPublicKey encryptionPublicKey,
                         RSAPrivateKey encryptionPrivateKey) {
}
