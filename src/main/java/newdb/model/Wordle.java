package newdb.model;

public class Wordle {

    private int userId;
    private int gamesPlayed;
    private int wins;
    private int fastestTime;
    private int highestStreak;
    private int currentStreak;

    public Wordle(int userId, int gamesPlayed, int wins, int fastestTime, int highestStreak, int currentStreak) {
        this.userId = userId;
        this.gamesPlayed = gamesPlayed;
        this.wins = wins;
        this.fastestTime = fastestTime;
        this.highestStreak = highestStreak;
        this.currentStreak = currentStreak;
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
}
