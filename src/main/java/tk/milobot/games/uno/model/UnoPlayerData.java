package tk.milobot.games.uno.model;

import net.dv8tion.jda.api.entities.User;

import java.util.List;

public class UnoPlayerData {

    private final User user;
    private final List<UnoCard> hand;
    private int totalCardsPlayed;
    private int totalCardsDrawn;
    private int timeSpentOnTurn;

    public UnoPlayerData(List<UnoCard> hand, User user) {
        this.user = user;
        this.totalCardsPlayed = 0;
        this.totalCardsDrawn = 0;
        this.hand = hand;
    }

    public UnoPlayerData(List<UnoCard> hand) {
        this.user = null;
        this.totalCardsPlayed = 0;
        this.totalCardsDrawn = 0;
        this.hand = hand;
    }

    public User getUser() {
        return user;
    }

    public int getTotalCardsPlayed() {
        return totalCardsPlayed;
    }

    public int getTotalCardsDrawn() {
        return totalCardsDrawn;
    }

    public void incrementTotalCardsPlayed() {
        this.totalCardsPlayed++;
    }

    public void incrementTotalCardsDrawn(int amount) {
        this.totalCardsDrawn += amount;
    }

    public List<UnoCard> getHand() {
        return hand;
    }

    public void incrementTimeSpentOnTurn(int time) {
        this.timeSpentOnTurn += time;
    }

    public int getTimeSpentOnTurn() {
        return timeSpentOnTurn;
    }
}