package models.hungergames;

import games.HungerGames;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JsePlatform;
import utility.Config;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static games.HungerGames.fileLoadHack;

public class ItemTest {

    List<Item> items;
    HungerGames game;
    Player player;
    Player victim;

    @BeforeEach
    void setUp() {
        this.game = new HungerGames();

        Globals globals = JsePlatform.standardGlobals();
        LuaValue gameLua = CoerceJavaToLua.coerce(game);
        globals.set("game", gameLua);

        this.items  = loadAllItems(globals);
        this.player = new Player("Player", "1");
        this.victim = new Player("Victim", "2");

        game.addPlayer(this.player);
        game.addPlayer(this.victim);

        Assertions.assertEquals(6, items.size());
    }

    @AfterEach
    void tearDown() {
        this.game = null;
        this.player = null;
        this.victim = null;
        this.items = null;

        this.game = new HungerGames();

        Globals globals = JsePlatform.standardGlobals();
        LuaValue gameLua = CoerceJavaToLua.coerce(game);
        globals.set("game", gameLua);

        this.items  = loadAllItems(globals);
        this.player = new Player("Player", "1");
        this.victim = new Player("Victim", "2");

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @Test
    public void testApple() {
        Item apple = items.get(0);

        this.player.damage(10);
        this.player.useItem(apple);

        Assertions.assertEquals(100, player.getHealth());
        Assertions.assertEquals(10, player.getHealingDone());
    }

    @Test
    public void testBandAid() {
        Item bandAid = items.get(1);

        this.player.damage(30);
        this.player.useItem(bandAid);

        Assertions.assertEquals(100, player.getHealth());
    }

    @Test
    public void testBomb() {
        Item bomb = items.get(2);

        this.player.useItem(bomb);

        Assertions.assertEquals(60, victim.getHealth());

        this.player.useItem(bomb);
        this.player.useItem(bomb);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(100, this.player.getDamageDone());
    }

    @Test
    public void testInfinityGauntlet() {
        Item infinityGauntlet = items.get(3);

        this.player.useItem(infinityGauntlet);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
    }

    @Test
    public void testSword() {
        Item sword = items.get(4);

        this.player.useItem(sword);

        Assertions.assertEquals(80, victim.getHealth());

        this.victim.damage(70);
        this.player.useItem(sword);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(30, this.player.getDamageDone());
        Assertions.assertEquals(1, this.player.getKills());
    }

    @Test
    public void testTotem() {
        Item totem = items.get(5);
        this.player.addItem(totem);
        this.player.onDeath();

        Assertions.assertEquals(2, game.getAlivePlayers().size());

        this.player.onDeath();

        Assertions.assertEquals(1, game.getAlivePlayers().size());
    }

    private @NotNull ArrayList<Item> loadAllItems(Globals globals) {
        ArrayList<Item> items = new ArrayList<>();
        try {
            URI uri = getClass().getResource(Config.getInstance().getHungerGamesPath() + "/items").toURI();
            fileLoadHack(uri);
            try (Stream<Path> paths = Files.walk(Paths.get(uri))) {
                paths
                        .filter(Files::isRegularFile)
                        .forEach((file) -> {
                            try {
                                items.add(new Item(globals, new String(Files.readAllBytes(file))));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        });
            }
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return items;
    }

}
