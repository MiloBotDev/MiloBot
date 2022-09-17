package games;

import models.cards.CardDeck;
import models.cards.UnoCard;
import net.dv8tion.jda.api.entities.User;

import java.util.ArrayList;
import java.util.List;

public class Uno {

    private static final ArrayList<Uno> unoGames = new ArrayList<>();
    private final List<User> players;
    private final CardDeck<UnoCard> deck;

    public Uno(List<User> players, CardDeck<UnoCard> deck) {
        this.players = players;
        this.deck = deck;
    }

}
