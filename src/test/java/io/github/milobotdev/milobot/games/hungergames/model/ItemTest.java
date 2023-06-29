package io.github.milobotdev.milobot.games.hungergames.model;

import io.github.milobotdev.milobot.games.hungergames.HungerGames;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

class ItemTest {

    static final int TEST_DAMAGE = 50;
    HungerGames hg = new HungerGames();

    @BeforeEach
    void setUp() {
        hg = new HungerGames();
        hg.addPlayer(new Player("Player 1", 1));
        hg.addPlayer(new Player("Player 2", 2));
    }

    @Test
    void testPear() {
        hg.getItemByName("pear").ifPresentOrElse(pear -> {
            Player player1 = hg.getPlayers().get(0);
            int healing = pear.getHeal();
            player1.damage(TEST_DAMAGE);
            player1.useItem(pear);
            player1.useItem(pear);

            assertEquals(healing * 2, player1.getHealingDone());
            assertEquals((Player.PLAYER_MAX_HEALTH - TEST_DAMAGE) + (2L * healing), player1.getHealth());
        }, () -> fail("Could not load that item."));
    }

    @Test
    void testApple() {
        hg.getItemByName("apple").ifPresentOrElse(apple -> {
            Player player1 = hg.getPlayers().get(0);
            int healing = apple.getHeal();
            player1.damage(TEST_DAMAGE);
            player1.useItem(apple);
            player1.useItem(apple);

            assertEquals(healing * 2, player1.getHealingDone());
            assertEquals((Player.PLAYER_MAX_HEALTH - TEST_DAMAGE) + (2L * healing), player1.getHealth());
        }, () -> fail("Could not load that item."));
    }

    @Test
    void testBandAid() {
        hg.getItemByName("band aid").ifPresentOrElse(bandAid -> {
            Player player1 = hg.getPlayers().get(0);
            int healing = bandAid.getHeal();
            player1.damage(TEST_DAMAGE);
            player1.damage(TEST_DAMAGE);
            player1.useItem(bandAid);
            player1.useItem(bandAid);

            assertEquals(healing * 2, player1.getHealingDone());
            assertEquals((Player.PLAYER_MAX_HEALTH - TEST_DAMAGE - TEST_DAMAGE) + (2L * healing), player1.getHealth());
        }, () -> fail("Could not load that item."));
    }

    @Test
    void testIcedCoffee() {
        hg.getItemByName("iced coffee").ifPresentOrElse(icedCoffee -> {
            Player player1 = hg.getPlayers().get(0);
            int healing = icedCoffee.getHeal();
            player1.damage(TEST_DAMAGE);
            player1.useItem(icedCoffee);
            player1.useItem(icedCoffee);

            assertEquals(healing * 2, player1.getHealingDone());
            assertEquals((Player.PLAYER_MAX_HEALTH - TEST_DAMAGE) + (2L * healing), player1.getHealth());
        }, () -> fail("Could not load that item."));
    }

    @Test
    void testBoba() {
        hg.getItemByName("boba").ifPresentOrElse(boba -> {
            Player player1 = hg.getPlayers().get(0);
            int healing = boba.getHeal();
            player1.damage(TEST_DAMAGE);
            player1.useItem(boba);
            player1.useItem(boba);

            assertEquals(healing * 2, player1.getHealingDone());
            assertEquals((Player.PLAYER_MAX_HEALTH - TEST_DAMAGE) + (2L * healing), player1.getHealth());
        }, () -> fail("Could not load that item."));
    }

    @Test
    void testBattleAxe() {
        hg.getItemByName("battleaxe").ifPresentOrElse(battleAxe -> {
            int damage = battleAxe.getDamage();
            Player player1 = hg.getPlayers().get(0);
            Player player2 = hg.getPlayers().get(1);
            player1.useItem(battleAxe);
            player1.useItem(battleAxe);

            assertEquals(damage * 2, player1.getDamageDone());
            assertEquals(damage * 2, player2.getDamageTaken());
        }, () -> fail("Could not load that item."));
    }

}