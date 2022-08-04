package models.hungergames;

import games.HungerGames;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ItemTest {

    HungerGames game;
    Player player;
    Player victim;

    @BeforeEach
    void setUp() {
        this.game = new HungerGames();

        this.player = new Player("Player", "1");
        this.victim = new Player("Victim", "2");

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @AfterEach
    void tearDown() {
        this.game = null;
        this.player = null;
        this.victim = null;

        this.game = new HungerGames();
        this.player = new Player("Player", "1");
        this.victim = new Player("Victim", "2");

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @Test
    public void testApple() {
        System.out.println(game);
        Item apple = game.getItemByName("apple").orElseThrow(() -> new RuntimeException("apple not found"));

        this.player.damage(10);
        this.player.useItem(apple);

        Assertions.assertEquals(100, player.getHealth());
        Assertions.assertEquals(10, player.getHealingDone());
    }

    @Test
    public void testBandAid() {
        Item bandAid = game.getItemByName("band aid").orElseThrow(() -> new RuntimeException("band aid not found"));

        this.player.damage(30);
        this.player.useItem(bandAid);

        Assertions.assertEquals(100, player.getHealth());
    }

    @Test
    public void testBomb() {
        Item bomb = game.getItemByName("bomb").orElseThrow(() -> new RuntimeException("bomb not found"));

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
        Item infinityGauntlet = game.getItemByName("infinity gauntlet").orElseThrow(() -> new RuntimeException("infinity gauntlet not found"));

        this.player.useItem(infinityGauntlet);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
    }

    @Test
    public void testSword() {
        Item sword = game.getItemByName("sword").orElseThrow(() -> new RuntimeException("sword not found"));

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
        Item totem = game.getItemByName("totem of not being dead").orElseThrow(() -> new RuntimeException("totem not found"));

        this.player.addItem(totem);
        this.player.onDeath();

        Assertions.assertEquals(2, game.getAlivePlayers().size());

        this.player.onDeath();

        Assertions.assertEquals(1, game.getAlivePlayers().size());
    }

    @Test
    public void testBow() {
        Item bow = game.getItemByName("bow").orElseThrow(() -> new RuntimeException("bow not found"));

        this.player.useItem(bow);

        Assertions.assertEquals(70, victim.getHealth());

        this.victim.damage(50);
        this.player.useItem(bow);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(50, this.player.getDamageDone());
        Assertions.assertEquals(1, this.player.getKills());
    }

    @Test
    public void testGun() {
        Item gun = game.getItemByName("gun").orElseThrow(() -> new RuntimeException("gun not found"));

        this.player.useItem(gun);

        Assertions.assertEquals(60, victim.getHealth());

        this.victim.damage(50);
        this.player.useItem(gun);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(50, this.player.getDamageDone());
    }

}
