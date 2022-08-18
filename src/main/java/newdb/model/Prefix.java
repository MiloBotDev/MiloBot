package newdb.model;

public class Prefix {
    private final int id;
    private final long guildId;
    private String prefix;

    public Prefix(long serverId, String prefix) {
        this.id = -1;
        this.guildId = serverId;
        this.prefix = prefix;
    }

    public Prefix(int id, long serverId, String prefix) {
        this.id = id;
        this.guildId = serverId;
        this.prefix = prefix;
    }

    public int getId() {
        return id;
    }

    public long getGuildId() {
        return guildId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
