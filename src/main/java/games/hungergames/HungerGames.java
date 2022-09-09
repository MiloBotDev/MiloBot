package games.hungergames;

import models.LobbyEntry;
import games.hungergames.models.Event;
import games.hungergames.models.Item;
import games.hungergames.models.Player;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private final Map<Integer, Map<List<String>, List<Player>>> roundData;
    private final List<Item> items;
    private final List<Event> events;
    private final long startTime;
    private List<String> messages;
    private boolean startedGame;
    private Player winner;
    private final Logger logger = LoggerFactory.getLogger(HungerGames.class);

    public HungerGames() {
        this.startedGame = false;
        this.startTime = System.nanoTime();
        this.players = new ArrayList<>();
        this.alivePlayers = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.items = new ArrayList<>();
        this.roundData = new HashMap<>();
        this.events = new ArrayList<>();

        Globals globals = getGlobals();
        loadAllItems(globals);
        loadAllEvents(globals);
    }

    public HungerGames(List<LobbyEntry> playersFromLobby) {
        this.startedGame = false;
        this.startTime = System.nanoTime();
        this.players = new ArrayList<>();
        this.alivePlayers = new ArrayList<>();
        this.messages = new ArrayList<>();
        this.items = new ArrayList<>();
        this.roundData = new HashMap<>();
        this.events = new ArrayList<>();

        playersFromLobby.forEach(newLobbyEntry -> addPlayer(new Player(newLobbyEntry.getUserName(), newLobbyEntry.getUserId())));

        Globals globals = getGlobals();
        loadAllItems(globals);
        loadAllEvents(globals);
    }

    public static void fileLoadHack(@NotNull URI uri) throws IOException {
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

    public Event getRandomEvent() {
        int total = events
                .stream()
                .map(Event::getRarity)
                .reduce(0, Integer::sum);

        Random rand = new Random();
        int chosen = rand.nextInt(total);
        int current = 0;
        for (Event event : events) {
            current += event.getRarity();
            if (current > chosen) {
                return event;
            }
        }

        return null; // should not happen
    }

    public void startGame() {
        this.startedGame = true;

        int round = 1;
        List<Player> playersAliveInRound;
        while (this.alivePlayers.size() > 1) {
            for (Player player : this.players) {
                if (this.alivePlayers.contains(player)) {
                    player.doAction();
                }
            }

            if (this.alivePlayers.size() == 1) {
                Player player = this.alivePlayers.get(0);
                log(String.format("%s has won the game!", player.getUserName()));
                this.winner = player;
            }

            playersAliveInRound = new ArrayList<>();
            for (Player player : this.alivePlayers) {
                playersAliveInRound.add(player.clone());
            }

            this.roundData.put(round, Map.of(this.messages, playersAliveInRound));
            this.messages = new ArrayList<>();

            round++;
        }
    }

    public Optional<Item> getItemByName(String name) {
        return this.items.stream().filter(item -> item.getName().equals(name)).findFirst();
    }

    public Optional<Event> getEventByName(String name) {
        return this.events.stream().filter(event -> event.getName().equals(name)).findFirst();
    }

    public Globals getGlobals() {
        Globals globals = JsePlatform.standardGlobals();
        LuaValue gameLua = CoerceJavaToLua.coerce(this);
        globals.set("game", gameLua);
        return globals;
    }

    private void loadAllItems(Globals globals) {
        try {
            URI uri = getClass().getResource(Config.getInstance().getHungerGamesPath() + "/items").toURI();
            fileLoadHack(uri);
            try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach((file) -> {
                            try {
                                this.items.add(new Item(globals, new String(Files.readAllBytes(file))));
                            } catch (IOException e) {
                                logger.error("Failed to load item from file: " + file, e);
                            }
                        });
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private void loadAllEvents(Globals globals) {
        try {
            URI uri = getClass().getResource(Config.getInstance().getHungerGamesPath() + "/events").toURI();
            fileLoadHack(uri);
            try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach((file) -> {
                            try {
                                this.events.add(new Event(globals, new String(Files.readAllBytes(file))));
                            } catch (IOException e) {
                                logger.error("Failed to event item from file: " + file, e);
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

    public void killPlayer(Player player) {
        this.alivePlayers.remove(player);
    }

    public List<Player> getAlivePlayers() {
        return alivePlayers;
    }

    public Map<Integer, Map<List<String>, List<Player>>> getRoundData() {
        return roundData;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Player getWinner() {
        return winner;
    }

}