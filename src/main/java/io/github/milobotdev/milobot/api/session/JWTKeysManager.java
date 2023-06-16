package io.github.milobotdev.milobot.api.session;

import io.github.milobotdev.milobot.database.dao.JWTKeysDao;

public class JWTKeysManager {

    private static JWTKeysManager instance = null;
    private final JWTKeysDao dao = JWTKeysDao.getInstance();
    private final JWTKeys keys;

    private JWTKeysManager() {
        keys = dao.getKeys();
    }

    public static synchronized JWTKeysManager getInstance() {
        if (instance == null) {
            instance = new JWTKeysManager();
        }
        return instance;
    }

    public JWTKeys getKeys() {
        return keys;
    }
}
