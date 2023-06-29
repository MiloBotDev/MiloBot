package io.github.milobotdev.milobot.commands.instance;

import java.util.ArrayList;
import java.util.List;

public class LobbyInstanceManager {

    private static LobbyInstanceManager instance = null;

    private final List<Long> usersInLobby = new ArrayList<>();

    private LobbyInstanceManager() {
    }

    public static LobbyInstanceManager getInstance() {
        if (instance == null) {
            instance = new LobbyInstanceManager();
        }
        return instance;
    }

    public void addUser(long userId) {
        usersInLobby.add(userId);
    }

    public void removeUser(long userId) {
        usersInLobby.remove(userId);
    }

    public boolean isUserInLobby(long userId) {
        return usersInLobby.contains(userId);
    }
}
