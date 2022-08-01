package games.hungergames;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import games.hungergames.models.Item;
import games.hungergames.models.Player;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import utility.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.*;
import java.util.stream.Stream;

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

    public List<Player> getAlivePlayers() {
        return alivePlayers;
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
    
    public void startGame() {
        this.startedGame = true;

        Globals globals = JsePlatform.standardGlobals();
        LuaValue gameLua = CoerceJavaToLua.coerce(this);
        globals.set("game", gameLua);

        // create items
        loadAllItems(globals);

        int round = 1;
        while (this.alivePlayers.size() > 1) {
            log(String.format("Round %d", round));
            round += 1;

            for (Player player : this.players) {
                if (this.alivePlayers.contains(player)) {
                    player.doAction();
                }
            }

            // reviving
            for (Player player : this.players) {
                if (!this.alivePlayers.contains(player)) {
                }
            }
        }

        if (this.alivePlayers.size() == 1) {
            log(String.format("%s has won the game!", this.alivePlayers.get(0).getUserName()));
        } else {
            log("no one has won the game :(");
        }

        for (String message : this.messages) {
            System.out.println(message);
        }
    }

    private void loadAllItems(Globals globals) {
        try {
            URI uri = getClass().getResource(Config.getInstance().getHungerGamesPath() + "/items").toURI();
            if ("jar".equals(uri.getScheme())) {
                for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                    if (provider.getScheme().equalsIgnoreCase("jar")) {
                        try {
                            provider.getFileSystem(uri);
                        } catch (FileSystemNotFoundException e) {
                            // in this case we need to initialize it first:
                            provider.newFileSystem(uri, Collections.emptyMap());
                        }
                    }
                }
            }
            try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                paths
                    .filter(Files::isRegularFile)
                    .forEach((file) -> {
                        try {
                            this.items.add(new Item(globals, new String(Files.readAllBytes(file))));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public void log(String message) {
        this.messages.add(message);
    }

    public static void main(String[] args) {
        HungerGames game = new HungerGames();
        game.addPlayer(new Player("PIEMEN", "69"));
        game.addPlayer(new Player("ruben", "43"));
        game.addPlayer(new Player("Mr. Obama", "102"));
        game.addPlayer(new Player("Morbious", "420"));
        game.startGame();
    }

}
