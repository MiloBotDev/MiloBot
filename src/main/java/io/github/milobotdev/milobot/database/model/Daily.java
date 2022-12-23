package io.github.milobotdev.milobot.database.model;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class Daily {

    private final int id;
    private final int userId;
    private Instant lastDailyTime;
    private int streak;
    private int totalClaimed;
    private int highestStreak;
    private int totalCurrencyClaimed;
    private int highestCurrencyClaimed;
    private int lowestCurrencyClaimed;

    public Daily(int id, int userId, Instant lastDailyTime, int streak, int totalClaimed, int highestStreak,
                 int totalCurrencyClaimed, int highestCurrencyClaimed, int lowestCurrencyClaimed) {
        this.id = id;
        this.userId = userId;
        this.lastDailyTime = lastDailyTime;
        this.streak = streak;
        this.totalClaimed = totalClaimed;
        this.highestStreak = highestStreak;
        this.totalCurrencyClaimed = totalCurrencyClaimed;
        this.highestCurrencyClaimed = highestCurrencyClaimed;
        this.lowestCurrencyClaimed = lowestCurrencyClaimed;
    }

    public Daily(int userId) {
        this.id = -1;
        this.userId = userId;
        this.streak = 0;
        this.totalClaimed = 0;
        this.highestStreak = 0;
        this.totalCurrencyClaimed = 0;
        this.highestCurrencyClaimed = 0;
        this.lowestCurrencyClaimed = 0;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    @Nullable
    public Instant getLastDailyTime() {
        return lastDailyTime;
    }

    public int getStreak() {
        return streak;
    }

    public int getTotalClaimed() {
        return totalClaimed;
    }

    public void incrementStreak() {
        streak++;
        if(this.streak > highestStreak) {
            this.highestStreak = this.streak;
        }
    }

    public void incrementTotalClaimed(int reward) {
        if (reward > this.highestCurrencyClaimed) {
            this.highestCurrencyClaimed = reward;
            if (reward < this.lowestCurrencyClaimed || this.lowestCurrencyClaimed == 0) {
                this.lowestCurrencyClaimed = reward;
            }
            this.totalCurrencyClaimed += reward;
            totalClaimed++;
        }
    }

    public void setLastDailyTime(Instant lastDailyTime) {
        this.lastDailyTime = lastDailyTime;
    }

    public void resetStreak() {
        streak = 1;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public int getTotalCurrencyClaimed() {
        return totalCurrencyClaimed;
    }

    public int getHighestCurrencyClaimed() {
        return highestCurrencyClaimed;
    }

    public int getLowestCurrencyClaimed() {
        return lowestCurrencyClaimed;
    }
}
