package models.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CardDeck<T> {

    private final List<T> cards;
    private final List<T> deck;

    public CardDeck(List<T> cards) {
        this.cards = cards;
        this.deck = new ArrayList<>();
        resetDeck();
    }

    public T drawCard() {
        return deck.remove(0);
    }

    public void fillDeck() {
        this.deck.addAll(cards);
    }

    public void resetDeck() {
        this.deck.clear();
        fillDeck();
        shuffleDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(this.deck);
    }
}
