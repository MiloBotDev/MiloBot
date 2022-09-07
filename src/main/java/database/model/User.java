package database.model;

public class User {

    private final long discordId;
    private int id = -1;
    private int currency = 0;
    private int level = 1;
    private int experience = 0;

    public User(int id, long discordId, int currency, int level, int experience) {
        this.id = id;
        this.discordId = discordId;
        this.currency = currency;
        this.level = level;
        this.experience = experience;
    }

    public User(long discordId) {
        this.discordId = discordId;
    }

    public User(long discordId, int currency, int level, int experience) {
        this.discordId = discordId;
        this.currency = currency;
        this.level = level;
        this.experience = experience;
    }

    public int getId() {
        return id;
    }

    public long getDiscordId() {
        return discordId;
    }

    public int getCurrency() {
        return currency;
    }

    public void setCurrency(int currency) {
        this.currency = currency;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public void incrementLevel() {
        this.level++;
    }

    public int getExperience() {
        return experience;
    }

    public void addExperience(int experience) {
        this.experience += experience;
    }

    @Override
    public String toString() {
        return "Id: " + id + " Discord id: " + discordId + " Currency: " + currency + " Level: " + level +
                " Experience: " + experience;
    }
}
