package database.model;

public class Wordle {

    private final int id;
    private final int userId;
    private int gamesPlayed;
    private int wins;
    private int fastestTime;
    private int highestStreak;
    private int currentStreak;

    public Wordle(int id, int userId, int gamesPlayed, int wins, int fastestTime, int highestStreak, int currentStreak) {
        this.id = id;
        this.userId = userId;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.fastestTime = fastestTime;
        this.highestStreak = highestStreak;
        this.currentStreak = currentStreak;
    }

    public Wordle(int userId, int gamesPlayed, int wins, int fastestTime, int highestStreak, int currentStreak) {
        this.id = -1;
        this.userId = userId;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.fastestTime = fastestTime;
        this.highestStreak = highestStreak;
        this.currentStreak = currentStreak;
    }

    public int getId() {
        return id;
    }

    public int getUserId() {
        return userId;
    }

    public int getGamesPlayed() {
        return gamesPlayed;
    }

    public int getWins() {
        return wins;
    }

    public int getFastestTime() {
        return fastestTime;
    }

    public int getHighestStreak() {
        return highestStreak;
    }

    public int getCurrentStreak() {
        return currentStreak;
    }

    public void addGame(boolean won, int time) {
        if (won) {
            currentStreak++;
            if (currentStreak > highestStreak) {
                highestStreak = currentStreak;
            }
            if (time < fastestTime) {
                fastestTime = time;
            }
            wins++;
        } else {
            currentStreak = 0;
        }
        gamesPlayed++;
    }
}
