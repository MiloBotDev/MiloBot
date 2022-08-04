package utility;

import models.LobbyEntry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Lobby {

    public final static Map<String, Lobby> lobbyInstances = new HashMap<>();

    private final LobbyEntry lobbyOwner;
    private final List<LobbyEntry> lobbyEntries;
    private final long startTime;

    private String lobbyId;

    public Lobby(String userId, String username) {
        this.lobbyEntries = new ArrayList<>();
        this.lobbyOwner = new LobbyEntry(userId, username);
        this.lobbyEntries.add(lobbyOwner);
        this.startTime = System.nanoTime();
        this.lobbyId = null;
    }

    public void initialize(String messageId) {
        lobbyInstances.put(messageId, this);
        this.lobbyId = messageId;
    }

    public void addPlayer(String userId, String username) {
        LobbyEntry lobbyEntry = new LobbyEntry(userId, username);
        this.lobbyEntries.add(lobbyEntry);
    }

    public void removePlayer(LobbyEntry lobbyEntry) {
        this.lobbyEntries.remove(lobbyEntry);
    }

    public String generateDescription() {
        StringBuilder sb = new StringBuilder();
        for(LobbyEntry lobbyEntry : lobbyEntries) {
            sb.append(lobbyEntry.username()).append("\n");
        }
        return sb.toString();
    }

    public void destroy() {
        if(this.lobbyId != null) {
            lobbyInstances.remove(lobbyId);
        }
    }

    public List<LobbyEntry> getPlayers() {
        return this.lobbyEntries;
    }

    public LobbyEntry getLobbyOwner() {
        return lobbyOwner;
    }

    public long getStartTime() {
        return startTime;
    }
}

