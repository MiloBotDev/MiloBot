package io.github.milobotdev.milobot.database.model;

public class Uno {

    private final int id;
    private final int userId;
    private int streak = 0;
    private int higheststreak = 0;
    private int totalGamesPlayed = 0;
    private int totalWins = 0;
    private int totalCardsPlayed = 0;
    private int totalCardsDrawn = 0;

    public Uno(int id, int userId, int streak, int higheststreak, int totalGames, int totalWins,
               int totalCardsPlayed, int totalCardsDrawn) {
        this.id = id;
        this.userId = userId;
        this.streak = streak;
        this.higheststreak = higheststreak;
        this.totalGamesPlayed = totalGames;
        this.totalWins = totalWins;
        this.totalCardsPlayed = totalCardsPlayed;
        this.totalCardsDrawn = totalCardsDrawn;
    }

    public Uno(int userId) {
        this.id = -1;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getStreak() {
        return streak;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getHigheststreak() {
        return higheststreak;
    }

    public int getTotalCardsPlayed() {
        return totalCardsPlayed;
    }

    public int getTotalCardsDrawn() {
        return totalCardsDrawn;
    }

    public void addGame(UnoGameResult result, int playedCards, int drawnCards) {
        if (result == UnoGameResult.WIN) {
            totalWins++;
            streak++;
            if (streak > higheststreak) {
                higheststreak = streak;
            }
        } else {
            streak = 0;
        }
        totalGamesPlayed++;
        totalCardsPlayed += playedCards;
        totalCardsDrawn += drawnCards;
    }

    public enum UnoGameResult {
        WIN, LOSS
    }

    @Override
    public String toString() {
        return "Uno{" +
                "id=" + id +
                ", userId=" + userId +
                ", streak=" + streak +
                ", higheststreak=" + higheststreak +
                ", totalGamesPlayed=" + totalGamesPlayed +
                ", totalWins=" + totalWins +
                ", totalCardsPlayed=" + totalCardsPlayed +
                ", totalCardsDrawn=" + totalCardsDrawn +
                '}';
    }
}
