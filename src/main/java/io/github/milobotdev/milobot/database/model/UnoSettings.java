package io.github.milobotdev.milobot.database.model;

public class UnoSettings {

    private final long discordId;
    private int id = -1;
    private int turnTimeLimit = 60;
    private int startingCardsAmount = 7;

    public UnoSettings(long discordId) {
        this.discordId = discordId;
    }

    public UnoSettings(long discordId, int id, int turnTimeLimit, int startingCardsAmount) {
        this.discordId = discordId;
        this.id = id;
        this.turnTimeLimit = turnTimeLimit;
        this.startingCardsAmount = startingCardsAmount;
    }

    public long getDiscordId() {
        return discordId;
    }

    public int getId() {
        return id;
    }

    public int getTurnTimeLimit() {
        return turnTimeLimit;
    }

    public int getStartingCardsAmount() {
        return startingCardsAmount;
    }

    @Override
    public String toString() {
        return "UnoSettings{" +
                "discordId=" + discordId +
                ", id=" + id +
                ", turnTimeLimit=" + turnTimeLimit +
                ", startingCardsAmount=" + startingCardsAmount +
                '}';
    }
}
