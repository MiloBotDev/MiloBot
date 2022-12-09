package tk.milobot.games.hungergames.model;

import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

public class LobbyEntry {

    private final long userId;
    private final User user;
    private final String username;
    private final String mention;
    private final boolean isBot;

    public LobbyEntry(long userId, String username, String mention) {
        this.userId = userId;
        this.username = username;
        this.mention = mention;
        this.isBot = false;
        this.user = null;
    }

    public LobbyEntry(@NotNull User user) {
        this.userId = user.getIdLong();
        this.username = user.getName();
        this.mention = user.getAsMention();
        this.isBot = user.isBot();
        this.user = user;
    }

    public LobbyEntry(String username) {
        this.userId = 0;
        this.username = username;
        this.mention = username;
        this.isBot = true;
        this.user = null;
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

    public User getUser() {
        return user;
    }

    @Override
    public String toString() {
        return "LobbyEntry{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", mention='" + mention + '\'' +
                ", isBot=" + isBot +
                '}';
    }
}