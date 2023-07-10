package io.github.milobotdev.milobot.api.session;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Date;

/**
 * Manages JWT tokens used for user sessions. JWTs are signed and encrypted.
 */
public class JWTManager {
    private final static JWTKeys keys = JWTKeysManager.getInstance().getKeys();

    /**
     * Generates a JWT with the given data.
     *
     * @param data The data to be stored in the JWT.
     * @return The JWT.
     * @throws JWTException If an error occurs while generating the JWT.
     */
    public static String generateJWT(String data) throws JWTException {
        try {
            // Create JWT
            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).build(),
                    new JWTClaimsSet.Builder()
                            .issueTime(new Date())
                            .notBeforeTime(new Date())
                            .expirationTime(new Date(new Date().getTime() + 15 * 60 * 1000))
                            .claim("data", data)
                            .build());

            // Sign the JWT
            signedJWT.sign(new RSASSASigner(JWTKeysManager.getInstance().getRSAKeys().signaturePrivateKey()));

            // Create JWE object with signed JWT as payload
            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.RSA_OAEP_256, EncryptionMethod.A256GCM)
                            .contentType("JWT") // required to indicate nested JWT
                            .build(),
                    new Payload(signedJWT));

            // Encrypt with the recipient's public key
            jweObject.encrypt(new RSAEncrypter(JWTKeysManager.getInstance().getRSAKeys().encryptionPublicKey()));

            // Serialize to JWE compact form
            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new JWTException("Exception encountered while creating JWT.", e);
        }
    }

    /**
     * Decrypts a JWT and returns the data stored in it.
     *
     * @param jwt The JWT to decrypt.
     * @return The data stored in the JWT.
     * @throws JWTException If an error occurs while decrypting the JWT.
     */
    public static String decryptJWT(String jwt) throws JWTException {
        try {
            // Parse the JWE string
            JWEObject jweObject = JWEObject.parse(jwt);

            // Decrypt with private key
            jweObject.decrypt(new RSADecrypter(JWTKeysManager.getInstance().getRSAKeys().encryptionPrivateKey()));

            // Extract payload
            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();

            // Check the signature
            if (!signedJWT.verify(new RSASSAVerifier(JWTKeysManager.getInstance().getRSAKeys().signaturePublicKey()))) {
                throw new JWTException("JWT signature verification failed.");
            }

            // Retrieve data
            return signedJWT.getJWTClaimsSet().getStringClaim("data");
        } catch (ParseException | JOSEException e) {
            throw new JWTException("Exception encountered while processing JWT.", e);
        }
    }
}
