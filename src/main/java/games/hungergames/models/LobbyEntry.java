package games.hungergames.models;

public class LobbyEntry {

    private final long userId;
    private final String username;
    private final String mention;
    private final boolean isBot;

    public LobbyEntry(long userId, String username, String mention) {
        this.userId = userId;
        this.username = username;
        this.mention = mention;
        this.isBot = false;
    }

    public LobbyEntry(String username) {
        this.userId = 0;
        this.username = username;
        this.mention = username;
        this.isBot = true;
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

    public boolean isBot() {
        return isBot;
    }
}