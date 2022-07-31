package games.hungergames;

import java.util.ArrayList;
import java.util.Objects;

public class HungerGames {

    public ArrayList<Player> players;
    public boolean startedGame;

    public HungerGames() {
        this.startedGame = false;
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
