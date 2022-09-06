package models;

public class LobbyEntry {

    private final String userId;
    private final String username;
    private final String mention;

    public LobbyEntry(String userId, String username, String mention) {
        this.userId = userId;
        this.username = username;
        this.mention = mention;
    }

    public LobbyEntry(String username) {
        this.userId = "#";
        this.username = username;
        this.mention = username;
    }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return username;
    }

    public String getMention() {
        return mention;
    }
}