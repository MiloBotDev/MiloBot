package models.dnd;

public class Monster {

    private final String name;
    private final String environment;
    private final String type;
    private final String mmPage;
    private final String xp;

    public Monster(String name, String environment, String type, String mmPage, String xp) {
        this.name = name;
        this.environment = environment;
        this.type = type;
        this.mmPage = mmPage;
        this.xp = xp;
    }

    public String getName() {
        return name;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getType() {
        return type;
    }

    public String getMmPage() {
        return mmPage;
    }

    public String getXp() {
        return xp;
    }
}
