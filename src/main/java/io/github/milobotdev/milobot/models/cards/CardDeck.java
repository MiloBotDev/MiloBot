package io.github.milobotdev.milobot.models.cards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Represents a deck of cards.
 * @param <T> The type of card.
 */
public class CardDeck<T> {

    private final List<T> cards;
    private final List<T> deck;

    public CardDeck(List<T> cards) {
        this.cards = cards;
        this.deck = new ArrayList<>();
        resetDeck();
    }

    /**
     * Draw a card from the deck.
     * @return Optional of the card drawn. The optional will be empty if the deck is empty.
     */
    public Optional<T> drawCard() {
        if(deck.size() == 0) {
            return Optional.empty();
        }
        return Optional.of(deck.remove(0));
    }

    /**
     * Draw multiple cards from the deck.
     * @param amount The amount of cards to draw.
     * @return A list of Optional cards. The Optional will be empty if the deck is empty.
     */
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

    /**
     * Fill the deck with cards.
     */
    public void fill() {
        this.deck.addAll(cards);
    }

    /**
     * Refill the deck with cards.
     * @param cardsToAdd The cards to add to the deck.
     */
    public void refill(List<T> cardsToAdd) {
        this.deck.addAll(cardsToAdd);
    }

    /**
     * Reset the deck to its original state.
     */
    public void resetDeck() {
        this.deck.clear();
        fill();
        shuffle();
    }

    /**
     * Shuffle the deck.
     */
    public void shuffle() {
        Collections.shuffle(this.deck);
    }

    /**
     * Remove a card from the deck.
     * @param card The card to remove.
     */
    public void removeCard(T card) {
        this.deck.remove(card);
    }

    /**
     * Clear the deck.
     */
    public void clear() {
        this.deck.clear();
    }
}
