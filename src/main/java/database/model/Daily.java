package database.model;

import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class Daily {
    private final int id;
    private final int userId;
    private Instant lastDailyTime;
    private int streak;
    private int totalClaimed;

    public Daily(int id, int userId, Instant lastDailyTime, int streak, int totalClaimed) {
        this.id = id;
        this.userId = userId;
        this.lastDailyTime = lastDailyTime;
        this.streak = streak;
        this.totalClaimed = totalClaimed;
    }

    public Daily(int userId) {
        this.id = -1;
        this.userId = userId;
        this.streak = 0;
        this.totalClaimed = 0;
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
    }

    public void incrementTotalClaimed() {
        totalClaimed++;
    }

    public void setLastDailyTime(Instant lastDailyTime) {
        this.lastDailyTime = lastDailyTime;
    }

    public void resetStreak() {
        streak = 1;
    }
}
