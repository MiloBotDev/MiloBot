package games.hungergames.models;

import games.hungergames.HungerGames;
import games.hungergames.models.Item;
import games.hungergames.models.Player;
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

        this.player = new Player("Player", 0);
        this.victim = new Player("Victim", 0);

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @AfterEach
    void tearDown() {
        this.game = null;
        this.player = null;
        this.victim = null;

        this.game = new HungerGames();
        this.player = new Player("Player", 0);
        this.victim = new Player("Victim", 0);

        game.addPlayer(this.player);
        game.addPlayer(this.victim);
    }

    @Test
    public void testApple() {
        Item apple = game.getItemByName("apple").orElseThrow(() -> new RuntimeException("apple not found"));

        this.player.damage(apple.getHeal() + 5);
        this.player.useItem(apple);
        this.player.useItem(apple);

        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH, player.getHealth());
        Assertions.assertEquals(apple.getHeal() + 5, player.getHealingDone());
    }

    @Test
    public void testBandAid() {
        Item bandAid = game.getItemByName("band aid").orElseThrow(() -> new RuntimeException("band aid not found"));

        this.player.damage(bandAid.getHeal() + 5);
        this.player.useItem(bandAid);
        this.player.useItem(bandAid);

        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH, player.getHealth());
        Assertions.assertEquals(bandAid.getHeal() + 5, player.getHealingDone());
    }

    @Test
    public void testBomb() {
        Item bomb = game.getItemByName("bomb").orElseThrow(() -> new RuntimeException("bomb not found"));

        this.player.useItem(bomb);

        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH - bomb.getDamage(), victim.getHealth());

        this.victim.heal(bomb.getDamage() - 5);
        this.victim.addItem(bomb);
        while (this.game.getAlivePlayers().size() != 1) {
            this.player.useItem(bomb);
        }

        Assertions.assertEquals(1, this.game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(bomb.getDamage(), this.player.getDamageTaken());
        Assertions.assertEquals(bomb.getDamage() + Player.PLAYER_MAX_HEALTH - 5, this.player.getDamageDone());
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

        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH - sword.getDamage(), victim.getHealth());

        this.victim.damage(5);
        while (this.game.getAlivePlayers().size() != 1) {
            this.player.useItem(sword);
        }

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH - 5, this.player.getDamageDone());
    }

    @Test
    public void testTotem() {
        Item totem = game.getItemByName("totem of not being dead").orElseThrow(() -> new RuntimeException("totem not found"));

        this.player.addItem(totem);
        this.player.onDeath();

        Assertions.assertEquals(2, game.getAlivePlayers().size());
        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH, this.player.getHealth());

        this.player.onDeath();

        Assertions.assertEquals(1, game.getAlivePlayers().size());
    }

    @Test
    public void testBow() {
        Item bow = game.getItemByName("bow").orElseThrow(() -> new RuntimeException("bow not found"));

        this.player.useItem(bow);

        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH - bow.getDamage(), victim.getHealth());

        this.victim.damage(5);
        while (this.game.getAlivePlayers().size() != 1) {
            this.player.useItem(bow);
        }

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(Player.PLAYER_MAX_HEALTH - 5, this.player.getDamageDone());
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

    @Test
    public void testSlingShot() {
        Item slingshot = game.getItemByName("slingshot").orElseThrow(() -> new RuntimeException("slingshot not found"));

        this.player.useItem(slingshot);

        Assertions.assertEquals(90, victim.getHealth());

        this.victim.damage(85);
        this.player.useItem(slingshot);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(15, this.player.getDamageDone());
    }

    @Test
    public void testRustyAxe() {
        Item rustyAxe = game.getItemByName("rusty axe").orElseThrow(() -> new RuntimeException("rusty axe not found"));

        this.player.useItem(rustyAxe);

        Assertions.assertEquals(85, victim.getHealth());

        this.victim.damage(80);
        this.player.useItem(rustyAxe);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(20, this.player.getDamageDone());
    }

    @Test
    public void testBattleAxe() {
        Item battleAxe = game.getItemByName("battleaxe").orElseThrow(() -> new RuntimeException("battleaxe not found"));

        this.player.useItem(battleAxe);

        Assertions.assertEquals(75, victim.getHealth());

        this.victim.damage(70);
        this.player.useItem(battleAxe);

        Assertions.assertEquals(1, game.getAlivePlayers().size());
        Assertions.assertEquals(1, this.player.getKills());
        Assertions.assertEquals(30, this.player.getDamageDone());
    }

    @Test
    public void testRocketLauncher() {

    }

    @Test
    public void testMorningstar() {

    }

    @Test
    public void testRustySword() {

    }

    @Test
    public void testBaseballBat() {

    }

    @Test
    public void testSpikedBaseballBat() {

    }

    @Test
    public void testNuclearBomb() {

    }

    @Test
    public void testPear() {
        Item pear = game.getItemByName("pear").orElseThrow(() -> new RuntimeException("pear not found"));

        this.player.damage(50);
        this.player.useItem(pear);

        Assertions.assertEquals(61, player.getHealth());
        Assertions.assertEquals(11, player.getHealingDone());
    }

    @Test
    public void testStrawberry() {
        Item strawberry = game.getItemByName("strawberry").orElseThrow(() -> new RuntimeException("strawberry not found"));

        this.player.damage(50);
        this.player.useItem(strawberry);

        Assertions.assertEquals(53, player.getHealth());
        Assertions.assertEquals(3, player.getHealingDone());
    }

    @Test
    public void testPineapple() {
        Item pineapple = game.getItemByName("pineapple").orElseThrow(() -> new RuntimeException("pineapple not found"));

        this.player.damage(50);
        this.player.useItem(pineapple);

        Assertions.assertEquals(57, player.getHealth());
        Assertions.assertEquals(7, player.getHealingDone());
    }

    @Test
    public void testIcedCoffee() {
        Item icedCoffee = game.getItemByName("iced coffee").orElseThrow(() -> new RuntimeException("iced coffee not found"));

        this.player.damage(50);
        this.player.useItem(icedCoffee);

        Assertions.assertEquals(57, player.getHealth());
        Assertions.assertEquals(7, player.getHealingDone());
    }

    @Test
    public void testPaper() {
        Item paper = game.getItemByName("paper").orElseThrow(() -> new RuntimeException("paper not found"));

        this.player.useItem(paper);
    }


}
