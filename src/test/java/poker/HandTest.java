package poker;

import games.Poker;
import models.cards.PlayingCards;
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

    List<List<List<PlayingCards>>> hands = List.of(
            // royal flush
            List.of(
                    List.of(
                            PlayingCards.TEN_OF_DIAMONDS,
                            PlayingCards.JACK_OF_DIAMONDS,
                            PlayingCards.QUEEN_OF_DIAMONDS,
                            PlayingCards.KING_OF_DIAMONDS,
                            PlayingCards.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_CLUBS,
                            PlayingCards.JACK_OF_CLUBS,
                            PlayingCards.QUEEN_OF_CLUBS,
                            PlayingCards.KING_OF_CLUBS,
                            PlayingCards.ACE_OF_CLUBS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_HEARTS,
                            PlayingCards.JACK_OF_HEARTS,
                            PlayingCards.QUEEN_OF_HEARTS,
                            PlayingCards.KING_OF_HEARTS,
                            PlayingCards.ACE_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_SPADES,
                            PlayingCards.JACK_OF_SPADES,
                            PlayingCards.QUEEN_OF_SPADES,
                            PlayingCards.KING_OF_SPADES,
                            PlayingCards.ACE_OF_SPADES
                    )
            ),
            // straight flush
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.SIX_OF_CLUBS,
                            PlayingCards.SEVEN_OF_CLUBS
                    ),
                    List.of(
                            PlayingCards.THREE_OF_DIAMONDS,
                            PlayingCards.FOUR_OF_DIAMONDS,
                            PlayingCards.FIVE_OF_DIAMONDS,
                            PlayingCards.SIX_OF_DIAMONDS,
                            PlayingCards.SEVEN_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.SIX_OF_HEARTS,
                            PlayingCards.SEVEN_OF_HEARTS,
                            PlayingCards.EIGHT_OF_HEARTS,
                            PlayingCards.NINE_OF_HEARTS,
                            PlayingCards.TEN_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.TWO_OF_HEARTS,
                            PlayingCards.THREE_OF_HEARTS,
                            PlayingCards.FOUR_OF_HEARTS,
                            PlayingCards.FIVE_OF_HEARTS,
                            PlayingCards.SIX_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.SEVEN_OF_CLUBS,
                            PlayingCards.EIGHT_OF_CLUBS,
                            PlayingCards.NINE_OF_CLUBS,
                            PlayingCards.TEN_OF_CLUBS,
                            PlayingCards.JACK_OF_CLUBS
                    )
            ),
            // four of a kind
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.THREE_OF_DIAMONDS,
                            PlayingCards.THREE_OF_HEARTS,
                            PlayingCards.THREE_OF_SPADES,
                            PlayingCards.FIVE_OF_CLUBS
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FOUR_OF_DIAMONDS,
                            PlayingCards.FOUR_OF_HEARTS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.FIVE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.KING_OF_SPADES,
                            PlayingCards.ACE_OF_CLUBS,
                            PlayingCards.ACE_OF_DIAMONDS,
                            PlayingCards.ACE_OF_HEARTS,
                            PlayingCards.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.TWO_OF_CLUBS,
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.TWO_OF_HEARTS,
                            PlayingCards.TWO_OF_SPADES,
                            PlayingCards.SIX_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.JACK_OF_CLUBS,
                            PlayingCards.JACK_OF_DIAMONDS,
                            PlayingCards.JACK_OF_HEARTS,
                            PlayingCards.JACK_OF_SPADES
                    )
            ),
            // full house
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.THREE_OF_HEARTS,
                            PlayingCards.THREE_OF_SPADES,
                            PlayingCards.SEVEN_OF_CLUBS,
                            PlayingCards.SEVEN_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FOUR_OF_HEARTS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.SIX_OF_CLUBS,
                            PlayingCards.SIX_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.THREE_OF_DIAMONDS,
                            PlayingCards.THREE_OF_SPADES,
                            PlayingCards.ACE_OF_DIAMONDS,
                            PlayingCards.ACE_OF_HEARTS,
                            PlayingCards.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.TWO_OF_CLUBS,
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.TWO_OF_HEARTS,
                            PlayingCards.SIX_OF_CLUBS,
                            PlayingCards.SIX_OF_SPADES
                    )
            ),
            // flush
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.SIX_OF_CLUBS,
                            PlayingCards.EIGHT_OF_CLUBS
                    ),
                    List.of(
                            PlayingCards.TWO_OF_HEARTS,
                            PlayingCards.SIX_OF_HEARTS,
                            PlayingCards.SEVEN_OF_HEARTS,
                            PlayingCards.JACK_OF_HEARTS,
                            PlayingCards.ACE_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.SIX_OF_SPADES,
                            PlayingCards.NINE_OF_SPADES,
                            PlayingCards.KING_OF_SPADES,
                            PlayingCards.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.SEVEN_OF_DIAMONDS,
                            PlayingCards.EIGHT_OF_DIAMONDS,
                            PlayingCards.NINE_OF_DIAMONDS,
                            PlayingCards.TEN_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.SIX_OF_DIAMONDS,
                            PlayingCards.EIGHT_OF_DIAMONDS,
                            PlayingCards.TEN_OF_DIAMONDS,
                            PlayingCards.KING_OF_DIAMONDS
                    )
            ),
            // straight
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.SIX_OF_CLUBS,
                            PlayingCards.SEVEN_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.SIX_OF_HEARTS,
                            PlayingCards.SEVEN_OF_HEARTS,
                            PlayingCards.EIGHT_OF_DIAMONDS,
                            PlayingCards.NINE_OF_HEARTS,
                            PlayingCards.TEN_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.TWO_OF_SPADES,
                            PlayingCards.THREE_OF_DIAMONDS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.FIVE_OF_HEARTS,
                            PlayingCards.SIX_OF_CLUBS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_SPADES,
                            PlayingCards.JACK_OF_SPADES,
                            PlayingCards.QUEEN_OF_SPADES,
                            PlayingCards.KING_OF_SPADES,
                            PlayingCards.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_DIAMONDS,
                            PlayingCards.JACK_OF_HEARTS,
                            PlayingCards.QUEEN_OF_DIAMONDS,
                            PlayingCards.KING_OF_DIAMONDS,
                            PlayingCards.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.NINE_OF_CLUBS,
                            PlayingCards.TEN_OF_DIAMONDS,
                            PlayingCards.JACK_OF_HEARTS,
                            PlayingCards.QUEEN_OF_CLUBS,
                            PlayingCards.KING_OF_DIAMONDS
                    )
            ),
            // three of a kind
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.THREE_OF_HEARTS,
                            PlayingCards.THREE_OF_SPADES,
                            PlayingCards.SEVEN_OF_CLUBS,
                            PlayingCards.NINE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FOUR_OF_HEARTS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.SIX_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FOUR_OF_HEARTS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.FIVE_OF_DIAMONDS,
                            PlayingCards.SIX_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.THREE_OF_DIAMONDS,
                            PlayingCards.JACK_OF_SPADES,
                            PlayingCards.ACE_OF_DIAMONDS,
                            PlayingCards.ACE_OF_HEARTS,
                            PlayingCards.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.THREE_OF_DIAMONDS,
                            PlayingCards.QUEEN_OF_DIAMONDS,
                            PlayingCards.QUEEN_OF_HEARTS,
                            PlayingCards.QUEEN_OF_SPADES,
                            PlayingCards.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.TWO_OF_CLUBS,
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.TWO_OF_HEARTS,
                            PlayingCards.EIGHT_OF_CLUBS,
                            PlayingCards.ACE_OF_SPADES
                    )
            ),
            // two pair
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_HEARTS,
                            PlayingCards.THREE_OF_SPADES,
                            PlayingCards.SIX_OF_SPADES,
                            PlayingCards.JACK_OF_DIAMONDS,
                            PlayingCards.JACK_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_SPADES,
                            PlayingCards.QUEEN_OF_HEARTS,
                            PlayingCards.QUEEN_OF_SPADES,
                            PlayingCards.ACE_OF_CLUBS,
                            PlayingCards.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_DIAMONDS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.FIVE_OF_DIAMONDS,
                            PlayingCards.SIX_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.SEVEN_OF_CLUBS,
                            PlayingCards.SEVEN_OF_HEARTS,
                            PlayingCards.ACE_OF_SPADES
                    )
            ),
            // pair
            List.of(
                    List.of(
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.THREE_OF_SPADES,
                            PlayingCards.SIX_OF_SPADES,
                            PlayingCards.JACK_OF_SPADES,
                            PlayingCards.KING_OF_HEARTS
                    ),
                    List.of(
                            PlayingCards.TEN_OF_SPADES,
                            PlayingCards.QUEEN_OF_HEARTS,
                            PlayingCards.KING_OF_SPADES,
                            PlayingCards.ACE_OF_CLUBS,
                            PlayingCards.ACE_OF_DIAMONDS
                    ),
                    List.of(
                            PlayingCards.TWO_OF_DIAMONDS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.FIVE_OF_SPADES,
                            PlayingCards.EIGHT_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FOUR_OF_SPADES,
                            PlayingCards.SEVEN_OF_CLUBS,
                            PlayingCards.EIGHT_OF_HEARTS,
                            PlayingCards.ACE_OF_SPADES
                    ),
                    List.of(
                            PlayingCards.TWO_OF_CLUBS,
                            PlayingCards.THREE_OF_CLUBS,
                            PlayingCards.FOUR_OF_CLUBS,
                            PlayingCards.FIVE_OF_CLUBS,
                            PlayingCards.FIVE_OF_SPADES
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
        for (int i=0; i<hands.size(); i++) {
            for (List<PlayingCards> hand: hands.get(i)) {
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
        for (int i=0; i<hands.size(); i++) {
            for (int j=0; j<hands.get(i).size(); j++) {
                List<PlayingCards> hand = hands.get(i).get(j);
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
