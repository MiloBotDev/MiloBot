package utility;

import models.LobbyEntry;

import java.util.*;

public class Lobby {

    public final static Map<String, Lobby> lobbyInstances = new HashMap<>();

    private final int maxPlayers;
    private final int minPlayers;
    private final LobbyEntry lobbyOwner;
    private final List<LobbyEntry> lobbyEntries;
    private final long startTime;

    private String lobbyId;


    public Lobby(String userId, String username) {
        this.maxPlayers = 15;
        this.minPlayers = 1;
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

    public void fillLobby() {
        String[] randomNames = {"Morbius", "Milo", "Jane Foster", "Captain America", "Walter White", "Jesse Pinkman",
                                "Obama", "Kanye West", "Bill Gates", "Elon Musk", "Steve Harrington", "John Oliver",
                                "Mother of Bram", "Nancy Wheeler", "Jonathan Byers", "Will Byers", "Vecna", "Darth Vader",
                                "Lilo & Stitch", "Your Mom", "Riot Games", "The Rock", "The Joker", "Batman", "The Flash",
                                "Jack Daniels"};
        List<String> randomNamesList = new ArrayList<>(Arrays.asList(randomNames));
        Collections.shuffle(randomNamesList);
        int count = 1;
        while(lobbyEntries.size() < maxPlayers) {
            addPlayer(String.format("#%d", count), randomNamesList.get(count - 1));
            count++;
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

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public int getMinPlayers() {
        return minPlayers;
    }
}

