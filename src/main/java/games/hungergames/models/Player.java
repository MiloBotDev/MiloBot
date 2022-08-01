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

    public void damage(int amount) {
        this.health -= amount;
        if (this.health <= 0) {
            this.health = 0;
        }
    }

    public void onDeath() {
        if (this.inventory.stream().anyMatch(item -> item.getName().equals("totem of not being dead"))) {
            this.game.log(String.format("%s has used their 'totem of not being dead', and is now back alive!", this.getUserName()));
            this.health = 50;
            this.removeItem(this.inventory.stream().filter(item -> item.getName().equals("totem of not being dead")).findFirst().get());
        } else {
            this.game.killPlayer(this);
        }
    }

    public void addItem(Item item) {
        this.inventory.add(item);
    }

    public void removeItem(Item item) {
        this.inventory.remove(item);
    }

//    public void doAction() {
//        Random rand = new Random();
//        if (rand.nextInt(10) < 7 && !this.inventory.isEmpty()) {
//            int itemNumber = rand.nextInt(this.inventory.size());
//            Item chosenItem = this.inventory.get(itemNumber);
//            chosenItem.use(this);
//        } else {
//            Item item = Item.getRandomItem();
//            this.inventory.add(item);
//            this.game.log(String.format("%s has found a %s.", this.userName, item.getName()));
//        }
//    }
}
