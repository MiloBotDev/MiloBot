package games.hungergames.models;

import games.hungergames.HungerGames;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player {

    private final String userName;
    private final String userId;
    private final List<Item> inventory;
    private long health;
    private HungerGames game;

    public Player(String userName, String userId) {
        this.userName = userName;
        this.userId = userId;
        this.inventory = new ArrayList<>();
        this.health = 100;
        this.game = null;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserId() {
        return userId;
    }

    public HungerGames getGame() {
        return game;
    }

    public long getHealth() {
        return health;
    }

    public void setGame(HungerGames game) {
        this.game = game;
    }

    public void heal(int amount) {
        this.health += amount;
        if (this.health > 100) {
            this.health = 100;
        }
    }

    public boolean damage(int amount) {
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
            return true;
        }
        return false;
    }

    public void onDeath() {
        for (Item item : this.inventory) {
            if (item.onDeath(this)) {
                return;
            }
        }
        this.game.killPlayer(this);
    }

    public void addItem(Item item) {
        this.inventory.add(item);
    }

    public void removeItem(String itemName) {
        for (Item item : this.inventory) {
            if (item.getName().equals(itemName)) {
                this.inventory.remove(item);
                return;
            }
        }
    }

    public void doAction() {
        Random rand = new Random();
        if (rand.nextInt(10) < 7 && !this.inventory.isEmpty()) {
            int itemNumber = rand.nextInt(this.inventory.size());
            Item chosenItem = this.inventory.get(itemNumber);
            chosenItem.onUse(this);
        } else {
            Item item = this.game.getRandomItem();
            this.inventory.add(item);
            this.game.log(String.format("%s has found a %s.", this.userName, item.getName()));
        }
    }
}
