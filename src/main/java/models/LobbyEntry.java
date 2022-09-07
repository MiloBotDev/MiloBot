package models;

public class LobbyEntry {

    private final long userId;
    private final String username;
    private final String mention;

    public LobbyEntry(long userId, String username, String mention) {
        this.userId = userId;
        this.username = username;
        this.mention = mention;
    }

    public LobbyEntry(String username) {
        this.userId = 0;
        this.username = username;
        this.mention = username;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return username;
    }

    public String getMention() {
        return mention;
    }
}