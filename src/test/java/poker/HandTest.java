package poker;

import games.Poker;
import models.cards.PlayingCard;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HandTest {
    private static final int ROYAL_FLUSH = 0b1000000000000000;
    private static final int STRAIGHT_FLUSH = 0b100000000000000;
    private static final int FOUR_OF_A_KIND = 0b10000000000000;
    private static final int FULL_HOUSE = 0b1000000000000;
    private static final int FLUSH = 0b100000000000;
    private static final int STRAIGHT = 0b10000000000;
    private static final int THREE_OF_A_KIND = 0b1000000000;
    private static final int TWO_PAIR = 0b100000000;
    private static final int PAIR = 0b10000000;

    List<List<List<PlayingCard>>> hands = List.of(
            // royal flush
            List.of(
                    List.of(
                            PlayingCard.TEN_OF_DIAMONDS,
                            PlayingCard.JACK_OF_DIAMONDS,
                            PlayingCard.QUEEN_OF_DIAMONDS,
                            PlayingCard.KING_OF_DIAMONDS,
                            PlayingCard.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_CLUBS,
                            PlayingCard.JACK_OF_CLUBS,
                            PlayingCard.QUEEN_OF_CLUBS,
                            PlayingCard.KING_OF_CLUBS,
                            PlayingCard.ACE_OF_CLUBS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_HEARTS,
                            PlayingCard.JACK_OF_HEARTS,
                            PlayingCard.QUEEN_OF_HEARTS,
                            PlayingCard.KING_OF_HEARTS,
                            PlayingCard.ACE_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_SPADES,
                            PlayingCard.JACK_OF_SPADES,
                            PlayingCard.QUEEN_OF_SPADES,
                            PlayingCard.KING_OF_SPADES,
                            PlayingCard.ACE_OF_SPADES
                    )
            ),
            // straight flush
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.SIX_OF_CLUBS,
                            PlayingCard.SEVEN_OF_CLUBS
                    ),
                    List.of(
                            PlayingCard.THREE_OF_DIAMONDS,
                            PlayingCard.FOUR_OF_DIAMONDS,
                            PlayingCard.FIVE_OF_DIAMONDS,
                            PlayingCard.SIX_OF_DIAMONDS,
                            PlayingCard.SEVEN_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.SIX_OF_HEARTS,
                            PlayingCard.SEVEN_OF_HEARTS,
                            PlayingCard.EIGHT_OF_HEARTS,
                            PlayingCard.NINE_OF_HEARTS,
                            PlayingCard.TEN_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.TWO_OF_HEARTS,
                            PlayingCard.THREE_OF_HEARTS,
                            PlayingCard.FOUR_OF_HEARTS,
                            PlayingCard.FIVE_OF_HEARTS,
                            PlayingCard.SIX_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.SEVEN_OF_CLUBS,
                            PlayingCard.EIGHT_OF_CLUBS,
                            PlayingCard.NINE_OF_CLUBS,
                            PlayingCard.TEN_OF_CLUBS,
                            PlayingCard.JACK_OF_CLUBS
                    )
            ),
            // four of a kind
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.THREE_OF_DIAMONDS,
                            PlayingCard.THREE_OF_HEARTS,
                            PlayingCard.THREE_OF_SPADES,
                            PlayingCard.FIVE_OF_CLUBS
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FOUR_OF_DIAMONDS,
                            PlayingCard.FOUR_OF_HEARTS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.FIVE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.KING_OF_SPADES,
                            PlayingCard.ACE_OF_CLUBS,
                            PlayingCard.ACE_OF_DIAMONDS,
                            PlayingCard.ACE_OF_HEARTS,
                            PlayingCard.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.TWO_OF_CLUBS,
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.TWO_OF_HEARTS,
                            PlayingCard.TWO_OF_SPADES,
                            PlayingCard.SIX_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.JACK_OF_CLUBS,
                            PlayingCard.JACK_OF_DIAMONDS,
                            PlayingCard.JACK_OF_HEARTS,
                            PlayingCard.JACK_OF_SPADES
                    )
            ),
            // full house
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.THREE_OF_HEARTS,
                            PlayingCard.THREE_OF_SPADES,
                            PlayingCard.SEVEN_OF_CLUBS,
                            PlayingCard.SEVEN_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FOUR_OF_HEARTS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.SIX_OF_CLUBS,
                            PlayingCard.SIX_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.THREE_OF_DIAMONDS,
                            PlayingCard.THREE_OF_SPADES,
                            PlayingCard.ACE_OF_DIAMONDS,
                            PlayingCard.ACE_OF_HEARTS,
                            PlayingCard.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.TWO_OF_CLUBS,
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.TWO_OF_HEARTS,
                            PlayingCard.SIX_OF_CLUBS,
                            PlayingCard.SIX_OF_SPADES
                    )
            ),
            // flush
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.SIX_OF_CLUBS,
                            PlayingCard.EIGHT_OF_CLUBS
                    ),
                    List.of(
                            PlayingCard.TWO_OF_HEARTS,
                            PlayingCard.SIX_OF_HEARTS,
                            PlayingCard.SEVEN_OF_HEARTS,
                            PlayingCard.JACK_OF_HEARTS,
                            PlayingCard.ACE_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.SIX_OF_SPADES,
                            PlayingCard.NINE_OF_SPADES,
                            PlayingCard.KING_OF_SPADES,
                            PlayingCard.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.SEVEN_OF_DIAMONDS,
                            PlayingCard.EIGHT_OF_DIAMONDS,
                            PlayingCard.NINE_OF_DIAMONDS,
                            PlayingCard.TEN_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.SIX_OF_DIAMONDS,
                            PlayingCard.EIGHT_OF_DIAMONDS,
                            PlayingCard.TEN_OF_DIAMONDS,
                            PlayingCard.KING_OF_DIAMONDS
                    )
            ),
            // straight
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.SIX_OF_CLUBS,
                            PlayingCard.SEVEN_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.SIX_OF_HEARTS,
                            PlayingCard.SEVEN_OF_HEARTS,
                            PlayingCard.EIGHT_OF_DIAMONDS,
                            PlayingCard.NINE_OF_HEARTS,
                            PlayingCard.TEN_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.TWO_OF_SPADES,
                            PlayingCard.THREE_OF_DIAMONDS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.FIVE_OF_HEARTS,
                            PlayingCard.SIX_OF_CLUBS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_SPADES,
                            PlayingCard.JACK_OF_SPADES,
                            PlayingCard.QUEEN_OF_SPADES,
                            PlayingCard.KING_OF_SPADES,
                            PlayingCard.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_DIAMONDS,
                            PlayingCard.JACK_OF_HEARTS,
                            PlayingCard.QUEEN_OF_DIAMONDS,
                            PlayingCard.KING_OF_DIAMONDS,
                            PlayingCard.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.NINE_OF_CLUBS,
                            PlayingCard.TEN_OF_DIAMONDS,
                            PlayingCard.JACK_OF_HEARTS,
                            PlayingCard.QUEEN_OF_CLUBS,
                            PlayingCard.KING_OF_DIAMONDS
                    )
            ),
            // three of a kind
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.THREE_OF_HEARTS,
                            PlayingCard.THREE_OF_SPADES,
                            PlayingCard.SEVEN_OF_CLUBS,
                            PlayingCard.NINE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FOUR_OF_HEARTS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.SIX_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FOUR_OF_HEARTS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.FIVE_OF_DIAMONDS,
                            PlayingCard.SIX_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.THREE_OF_DIAMONDS,
                            PlayingCard.JACK_OF_SPADES,
                            PlayingCard.ACE_OF_DIAMONDS,
                            PlayingCard.ACE_OF_HEARTS,
                            PlayingCard.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.THREE_OF_DIAMONDS,
                            PlayingCard.QUEEN_OF_DIAMONDS,
                            PlayingCard.QUEEN_OF_HEARTS,
                            PlayingCard.QUEEN_OF_SPADES,
                            PlayingCard.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.TWO_OF_CLUBS,
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.TWO_OF_HEARTS,
                            PlayingCard.EIGHT_OF_CLUBS,
                            PlayingCard.ACE_OF_SPADES
                    )
            ),
            // two pair
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_HEARTS,
                            PlayingCard.THREE_OF_SPADES,
                            PlayingCard.SIX_OF_SPADES,
                            PlayingCard.JACK_OF_DIAMONDS,
                            PlayingCard.JACK_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_SPADES,
                            PlayingCard.QUEEN_OF_HEARTS,
                            PlayingCard.QUEEN_OF_SPADES,
                            PlayingCard.ACE_OF_CLUBS,
                            PlayingCard.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_DIAMONDS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.FIVE_OF_DIAMONDS,
                            PlayingCard.SIX_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.SEVEN_OF_CLUBS,
                            PlayingCard.SEVEN_OF_HEARTS,
                            PlayingCard.ACE_OF_SPADES
                    )
            ),
            // pair
            List.of(
                    List.of(
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.THREE_OF_SPADES,
                            PlayingCard.SIX_OF_SPADES,
                            PlayingCard.JACK_OF_SPADES,
                            PlayingCard.KING_OF_HEARTS
                    ),
                    List.of(
                            PlayingCard.TEN_OF_SPADES,
                            PlayingCard.QUEEN_OF_HEARTS,
                            PlayingCard.KING_OF_SPADES,
                            PlayingCard.ACE_OF_CLUBS,
                            PlayingCard.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCard.TWO_OF_DIAMONDS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.FIVE_OF_SPADES,
                            PlayingCard.EIGHT_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FOUR_OF_SPADES,
                            PlayingCard.SEVEN_OF_CLUBS,
                            PlayingCard.EIGHT_OF_HEARTS,
                            PlayingCard.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCard.TWO_OF_CLUBS,
                            PlayingCard.THREE_OF_CLUBS,
                            PlayingCard.FOUR_OF_CLUBS,
                            PlayingCard.FIVE_OF_CLUBS,
                            PlayingCard.FIVE_OF_SPADES
                    )
            )
    );

    List<List<Integer>> highCards = List.of(
            List.of(),
            List.of(7, 7, 10, 6, 11),
            List.of(3, 4, 14, 2, 11),
            List.of(3, 4, 14, 2),
            List.of(8, 14, 14, 10, 13),
            List.of(7, 10, 6, 14, 14, 13),
            List.of(3, 4, 4, 14, 12, 2),
            List.of(11, 14, 5, 7),
            List.of(3, 14, 5, 4, 5)
    );

    @Test
    void testRoyalFlush() {
        int index = 0;
        for (int i = 0; i < hands.size(); i++) {
            for (List<PlayingCard> hand : hands.get(i)) {
                if (i == index) {
                    assertEquals(ROYAL_FLUSH, Poker.Hands.getHandValue(hand), "Royal flush: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & ROYAL_FLUSH,
                            "Not royal flush: " + hand);
                }
            }
        }
    }

    @Test
    void testStraightFlush() {
        int index = 1;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(STRAIGHT_FLUSH | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Straight flush: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & STRAIGHT_FLUSH,
                            "Not straight flush: " + hand);
                }
            }
        }
    }

    @Test
    void testFourOfAKind() {
        int index = 2;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(FOUR_OF_A_KIND | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Four of a kind: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & FOUR_OF_A_KIND,
                            "Not four of a kind: " + hand);
                }
            }
        }
    }

    @Test
    void testFullHouse() {
        int index = 3;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(FULL_HOUSE | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Full house: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & FULL_HOUSE,
                            "Not full house: " + hand);
                }
            }
        }
    }

    @Test
    void testFlush() {
        int index = 4;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(FLUSH | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Flush: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & FLUSH,
                            "Not flush: " + hand);
                }
            }
        }
    }

    @Test
    void testStraight() {
        int index = 5;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(STRAIGHT | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Straight: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & STRAIGHT,
                            "Not straight: " + hand);
                }
            }
        }
    }

    @Test
    void testThreeOfAKind() {
        int index = 6;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(THREE_OF_A_KIND | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Three of a kind: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & THREE_OF_A_KIND,
                            "Not three of a kind: " + hand);
                }
            }
        }
    }

    @Test
    void testTwoPair() {
        int index = 7;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(TWO_PAIR | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Two pair: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & TWO_PAIR,
                            "Two pair: " + hand);
                }
            }
        }
    }

    @Test
    void testPair() {
        int index = 8;
        for (int i = 0; i < hands.size(); i++) {
            for (int j = 0; j < hands.get(i).size(); j++) {
                List<PlayingCard> hand = hands.get(i).get(j);
                if (i == index) {
                    assertEquals(PAIR | highCards.get(i).get(j),
                            Poker.Hands.getHandValue(hand), "Pair: " + hand);
                } else {
                    assertEquals(0, Poker.Hands.getHandValue(hand) & PAIR,
                            "Ppair: " + hand);
                }
            }
        }
    }
}
