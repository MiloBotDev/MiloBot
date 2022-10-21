package models.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

    public List<Optional<T>> drawCards(int amount) {
        List<Optional<T>> drawnCards = new ArrayList<>();
        for(int i = 0; i < amount; i++) {
            if(deck.size() == 0) {
                drawnCards.add(Optional.empty());
            } else {
                drawnCards.add(Optional.of(deck.remove(0)));
            }
        }
        return drawnCards;
    }

    public void fillDeck() {
        this.deck.addAll(cards);
    }

    public void refreshDeck(List<T> cardsToAdd) {
        this.deck.addAll(cardsToAdd);
    }

    public void resetDeck() {
        this.deck.clear();
        fillDeck();
        shuffleDeck();
    }

    public void shuffleDeck() {
        Collections.shuffle(this.deck);
    }

    public void removeCard(T card) {
        this.deck.remove(card);
    }
}
