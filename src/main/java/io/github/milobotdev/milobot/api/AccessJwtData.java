package io.github.milobotdev.milobot.api;

import java.util.Date;

public record AccessJwtData(int sessionDataVersion, String accessToken, Date accessTokenExpiry, String refreshToken) {
    public AccessJwtData(String accessToken, Date accessTokenExpiry, String refreshToken) {
        this(1, accessToken, accessTokenExpiry, refreshToken);
    }
}
