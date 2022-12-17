package io.github.milobotdev.milobot.database.model;

public class Blackjack {

    private final int id;
    private final int userId;
    private boolean wonLastGame = false;
    private int streak = 0;
    private int totalGames = 0;
    private int totalWins = 0;
    private int totalDraws = 0;
    private int totalEarnings = 0;
    private int highestStreak = 0;

    public Blackjack(int id, int userId, boolean wonLastGame, int streak, int totalGames, int totalWins,
                     int totalDraws, int totalEarnings, int highestStreak) {
        this.id = id;
        this.userId = userId;
        this.wonLastGame = wonLastGame;
        this.streak = streak;
        this.totalGames = totalGames;
        this.totalWins = totalWins;
        this.totalDraws = totalDraws;
        this.totalEarnings = totalEarnings;
        this.highestStreak = highestStreak;
    }

    public Blackjack(int userId) {
        this.id = -1;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public boolean wonLastGame() {
        return wonLastGame;
    }

    public int getStreak() {
        return streak;
    }

    public int getTotalGames() {
        return totalGames;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public int getTotalDraws() {
        return totalDraws;
    }

    public int getTotalEarnings() {
        return totalEarnings;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public void addGame(BlackjackResult result, int earning) {
        wonLastGame = result == BlackjackResult.WIN;
        totalGames++;
        totalEarnings += earning;
        if (wonLastGame) {
            streak++;
            if (streak > highestStreak) {
                highestStreak = streak;
            }
            totalWins++;
        } else {
            streak = 0;
        }
        if (result == BlackjackResult.DRAW) {
            totalDraws++;
        }
    }

    public enum BlackjackResult {
        WIN, DRAW, LOSS
    }
}
