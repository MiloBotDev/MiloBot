package io.github.milobotdev.milobot.database.model;

public class HungerGames {

    private final int id;
    private final int userId;
    private int totalKills = 0;
    private int totalDamageDone = 0;
    private int totalDamageTaken = 0;
    private int totalHealingDone = 0;
    private int totalItemsCollected = 0;
    private int totalGamesPlayed = 0;
    private int totalWins = 0;

    public HungerGames(int id, int userId, int totalKills, int totalDamageDone, int totalDamageTaken,
                       int totalHealingDone, int totalItemsCollected, int totalGamesPlayed, int totalWins) {
        this.id = id;
        this.userId = userId;
        this.totalKills = totalKills;
        this.totalDamageDone = totalDamageDone;
        this.totalDamageTaken = totalDamageTaken;
        this.totalHealingDone = totalHealingDone;
        this.totalItemsCollected = totalItemsCollected;
        this.totalGamesPlayed = totalGamesPlayed;
        this.totalWins = totalWins;
    }

    public HungerGames(int userId) {
        this.id = -1;
        this.userId = userId;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getTotalKills() {
        return totalKills;
    }

    public int getTotalDamageDone() {
        return totalDamageDone;
    }

    public int getTotalDamageTaken() {
        return totalDamageTaken;
    }

    public int getTotalHealingDone() {
        return totalHealingDone;
    }

    public int getTotalItemsCollected() {
        return totalItemsCollected;
    }

    public int getTotalGamesPlayed() {
        return totalGamesPlayed;
    }

    public int getTotalWins() {
        return totalWins;
    }

    public void addGame(HungerGamesResult result, int kills, int damageDone, int damageTaken, int healingDone,
                        int itemsCollected) {
        totalKills += kills;
        totalDamageDone += damageDone;
        totalDamageTaken += damageTaken;
        totalHealingDone += healingDone;
        totalItemsCollected += itemsCollected;
        totalGamesPlayed++;
        if (result == HungerGamesResult.WIN) {
            totalWins++;
        }
    }

    public enum HungerGamesResult {
        WIN, LOSS, DRAW
    }
}
