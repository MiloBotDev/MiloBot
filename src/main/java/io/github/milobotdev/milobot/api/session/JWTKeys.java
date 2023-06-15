package io.github.milobotdev.milobot.api.session;

import io.github.milobotdev.milobot.database.dao.JWTKeysDao;

public class JWTKeys {

    private static JWTKeys instance = null;
    private final JWTKeysDao dao = JWTKeysDao.getInstance();

    private JWTKeys() {
    }

    public static synchronized JWTKeys getInstance() {
        if (instance == null) {
            instance = new JWTKeys();
        }
        return instance;
    }
}
