package games.hungergames;

import games.hungergames.models.Player;

import java.util.ArrayList;
import java.util.Objects;

public class HungerGames {

    private ArrayList<Player> players;
    private boolean startedGame;
    private final long startTime;

    public HungerGames() {
        this.startedGame = false;
        this.startTime = System.nanoTime();
    }

    public void addPlayer(Player player) {
        if (!startedGame) {
            players.add(player);
        }
    }

    public void removePlayer(String userId) {
        players.removeIf(player1 -> Objects.equals(player1.getUserId(), userId));
    }

    public void startGame() {
        this.startedGame = true;
    }

}
