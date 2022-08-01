package games.hungergames;

import games.hungergames.models.Item;
import games.hungergames.models.Player;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class HungerGames {

    private final List<Player> players;
    private final List<Player> alivePlayers;
    private final List<String> messages;
    private final List<Item> items;
    private boolean startedGame;
    private final long startTime;

    public HungerGames() {
        this.startedGame = false;
        this.startTime = System.nanoTime();
        this.players = new ArrayList<>();
        this.alivePlayers = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.items = new ArrayList<>();
    }

    public void addPlayer(Player player) {
        if (!this.startedGame) {
            this.players.add(player);
            this.alivePlayers.add(player);
            player.setGame(this);
        }
    }

    public void removePlayer(String userId) {
        if (!this.startedGame) {
            this.players.removeIf(player1 -> Objects.equals(player1.getUserId(), userId));
            this.alivePlayers.removeIf(player1 -> Objects.equals(player1.getUserId(), userId));
        }
    }

    public void killPlayer(Player player) {
        this.alivePlayers.remove(player);
    }

    public Player getRandomPlayer() {
        Random rand = new Random();
        return this.alivePlayers.get(rand.nextInt(this.alivePlayers.size()));
    }

    public Player getRandomPlayer(Player ignore) {
        Random rand = new Random();
        if (alivePlayers.size() == 1) {
            return null;
        }
        return this.alivePlayers.stream()
                .filter(player -> player != ignore)
                .toList()
                .get(rand.nextInt(this.alivePlayers.size() - 1));
    }

    public Item getRandomItem() {
        int total = items
                .stream()
                .map(Item::getRarity)
                .reduce(0, Integer::sum);

        Random rand = new Random();
        int chosen = rand.nextInt(total);
        int current = 0;
        for (Item item : items) {
            current += item.getRarity();
            if (current > chosen) {
                return item;
            }
        }

        return null; // should not happen
    }

//    public void startGame() {
//        this.startedGame = true;
//
//        Globals globals = JsePlatform.standardGlobals();
//        LuaValue gameLua = CoerceJavaToLua.coerce(this);
//        globals.set("game", gameLua);
//
//        int round = 1;
//        while (this.alivePlayers.size() > 1) {
//            log(String.format("Round %d", round));
//            round += 1;
//
//            for (Player player : this.players) {
//                if (this.alivePlayers.contains(player)) {
//                    player.doAction();
//                }
//            }
//
//            // reviving
//            for (Player player : this.players) {
//                if (!this.alivePlayers.contains(player)) {
//                }
//            }
//        }
//
//        if (this.alivePlayers.size() == 1) {
//            log(String.format("%s has won the game!", this.alivePlayers.get(0).getUserName()));
//        } else {
//            log("no one has won the game :(");
//        }
//
//        for (String message : this.messages) {
//            System.out.println(message);
//        }
//    }

    public void log(String message) {
        this.messages.add(message);
    }

    public static void main(String[] args) {
        HungerGames game = new HungerGames();
        Player piemen = new Player("PIEMEN", "69");
        game.addPlayer(new Player("ruben", "43"));
        game.addPlayer(new Player("Mr. Obama", "102"));
        game.addPlayer(new Player("Morbious", "420"));

        System.out.println();

        for (String message : game.messages) {
            System.out.println(message);
        }

//        HungerGames game = new HungerGames();
//        game.addPlayer(new Player("PIEMEN", "69"));
//        game.addPlayer(new Player("ruben", "43"));
//        game.addPlayer(new Player("Mr. Obama", "102"));
//        game.addPlayer(new Player("Morbious", "420"));
//        game.startGame();
    }

}
