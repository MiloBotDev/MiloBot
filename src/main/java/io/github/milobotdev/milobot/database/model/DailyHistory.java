package io.github.milobotdev.milobot.database.model;

import java.time.Instant;

public class DailyHistory {

    private final int id;
    private final int userId;
    private final Instant time;
    private final int amount;


    public DailyHistory(int userId, Instant time, int amount) {
        this.userId = userId;
        this.time = time;
        this.amount = amount;
        this.id = -1;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public Instant getTime() {
        return time;
    }

    public int getAmount() {
        return amount;
    }
}
