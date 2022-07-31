package games.hungergames.models;

public enum ItemType {
    WEAPON("weapon"),
    FOOD("food"),
    USABLE("usable");

    private final String name;

    ItemType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
