package io.github.milobotdev.milobot.api.session;

/**
 * Represents an exception that occurs when handling JWTs.
 */
public class JWTException extends Exception {
    public JWTException(String s, Exception e) {
        super(s, e);
    }

    public JWTException(String s) {
        super(s);
    }
}
