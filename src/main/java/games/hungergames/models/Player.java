package games.hungergames.models;

import games.hungergames.HungerGames;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Player implements Cloneable {

    public static final long PLAYER_MAX_HEALTH = 200;

    private final String username;
    private final String userId;
    private List<Item> inventory;
    private long health;
    private HungerGames game;

    private int kills;
    private int itemsCollected;
    private int damageDone;
    private int damageTaken;
    private int healingDone;

    public Player(String username, String userId) {
        this.username = username;
        this.userId = userId;
        this.inventory = new ArrayList<>();
        this.health = PLAYER_MAX_HEALTH;
        this.game = null;

        this.kills = 0;
        this.itemsCollected = 0;
        this.damageDone = 0;
        this.damageTaken = 0;
        this.healingDone = 0;
    }

    public void heal(int amount) {
        long remainingHealth = PLAYER_MAX_HEALTH - this.health;
        this.health += amount;
        if (this.health > PLAYER_MAX_HEALTH) {
            this.health = PLAYER_MAX_HEALTH;
            this.healingDone += remainingHealth;
        } else {
            this.healingDone += amount;
        }
    }

    public boolean damage(int amount) {
        this.health -= amount;
        this.damageTaken += amount;
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
        this.health = 0;
    }

    public void addItem(Item item) {
        this.inventory.add(item);
        this.itemsCollected++;
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
        int odd = rand.nextInt(10);
        // 60% to use an item
        if (odd < 6 && !this.inventory.isEmpty()) {
            int itemNumber = rand.nextInt(this.inventory.size());
            Item chosenItem = this.inventory.get(itemNumber);
            chosenItem.onUse(this);
            // 10% for an event to occur
        } else if (odd < 7 && !this.inventory.isEmpty()) {
            Event event = this.game.getRandomEvent();
            event.onTrigger(this);
            // 30% chance to find an item
        } else {
            Item item = this.game.getRandomItem();
            this.inventory.add(item);
            this.game.log(String.format("%s has found a %s.", this.username, item.getName()));
        }
    }

    public void useItem(@NotNull Item item) {
        item.onUse(this);
    }

    public void triggerEvent(@NotNull Event event) {
        event.onTrigger(this);
    }

    public void reset() {
        this.kills = 0;
        this.itemsCollected = 0;
        this.damageDone = 0;
        this.damageTaken = 0;
        this.healingDone = 0;
        this.inventory = new ArrayList<>();
    }

    @Override
    public Player clone() {
        Player clone;
        try {
            clone = (Player) super.clone();
            List<Item> inventoryClone = new ArrayList<>(this.inventory.size());
            inventoryClone.addAll(this.inventory);
            clone.inventory = inventoryClone;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getUserName() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public HungerGames getGame() {
        return game;
    }

    public void setGame(HungerGames game) {
        this.game = game;
    }

    public long getHealth() {
        return health;
    }

    public List<Item> getInventory() {
        return inventory;
    }

    public int getKills() {
        return kills;
    }

    public int getItemsCollected() {
        return itemsCollected;
    }

    public int getDamageDone() {
        return damageDone;
    }

    public int getDamageTaken() {
        return damageTaken;
    }

    public int getHealingDone() {
        return healingDone;
    }

    public void addKill() {
        this.kills++;
    }

    public void addDamageDone(int damage) {
        this.damageDone += damage;
    }
}
