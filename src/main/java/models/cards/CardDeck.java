package models.cards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CardDeck {

    private final List<PlayingCards> cards;

    public CardDeck() {
        this.cards = new ArrayList<>();
        resetDeck();
    }

    public PlayingCards drawCard() {
        return cards.remove(0);
    }

    public void fillDeck() {
        this.cards.addAll(Arrays.asList(PlayingCards.values()));
    }

    public void resetDeck() {
        this.cards.clear();
        fillDeck();
        shuffleDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(this.cards);
    }
}
