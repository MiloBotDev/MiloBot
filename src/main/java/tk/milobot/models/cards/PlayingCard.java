package tk.milobot.models.cards;

import tk.milobot.models.CustomEmoji;

import static tk.milobot.models.CustomEmoji.*;

public enum PlayingCard {

    ACE_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_ACE.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.ACE),
    TWO_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_TWO.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.TWO),
    THREE_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_THREE.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.THREE),
    FOUR_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_FOUR.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.FOUR),
    FIVE_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_FIVE.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.FIVE),
    SIX_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_SIX.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.SIX),
    SEVEN_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_SEVEN.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.SEVEN),
    EIGHT_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_EIGHT.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.EIGHT),
    NINE_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_NINE.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.NINE),
    TEN_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_TEN.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.TEN),
    JACK_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_JACK.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.JACK),
    QUEEN_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_QUEEN.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.QUEEN),
    KING_OF_CLUBS(String.format("%s\n%s", CustomEmoji.BLACK_KING.getEmoji(), CustomEmoji.CLUBS.getEmoji()), Suit.CLUBS, Rank.KING),
    ACE_OF_DIAMONDS(String.format("%s\n%s", RED_ACE.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.ACE),
    TWO_OF_DIAMONDS(String.format("%s\n%s", RED_TWO.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.TWO),
    THREE_OF_DIAMONDS(String.format("%s\n%s", RED_THREE.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.THREE),
    FOUR_OF_DIAMONDS(String.format("%s\n%s", RED_FOUR.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.FOUR),
    FIVE_OF_DIAMONDS(String.format("%s\n%s", RED_FIVE.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.FIVE),
    SIX_OF_DIAMONDS(String.format("%s\n%s", RED_SIX.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.SIX),
    SEVEN_OF_DIAMONDS(String.format("%s\n%s", RED_SEVEN.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.SEVEN),
    EIGHT_OF_DIAMONDS(String.format("%s\n%s", RED_EIGHT.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.EIGHT),
    NINE_OF_DIAMONDS(String.format("%s\n%s", RED_NINE.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.NINE),
    TEN_OF_DIAMONDS(String.format("%s\n%s", RED_TEN.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.TEN),
    JACK_OF_DIAMONDS(String.format("%s\n%s", RED_JACK.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.JACK),
    QUEEN_OF_DIAMONDS(String.format("%s\n%s", RED_QUEEN.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.QUEEN),
    KING_OF_DIAMONDS(String.format("%s\n%s", RED_KING.getEmoji(), CustomEmoji.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.KING),
    ACE_OF_HEARTS(String.format("%s\n%s", RED_ACE.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.ACE),
    TWO_OF_HEARTS(String.format("%s\n%s", RED_TWO.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.TWO),
    THREE_OF_HEARTS(String.format("%s\n%s", RED_THREE.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.THREE),
    FOUR_OF_HEARTS(String.format("%s\n%s", RED_FOUR.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.FOUR),
    FIVE_OF_HEARTS(String.format("%s\n%s", RED_FIVE.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.FIVE),
    SIX_OF_HEARTS(String.format("%s\n%s", RED_SIX.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.SIX),
    SEVEN_OF_HEARTS(String.format("%s\n%s", RED_SEVEN.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.SEVEN),
    EIGHT_OF_HEARTS(String.format("%s\n%s", RED_EIGHT.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.EIGHT),
    NINE_OF_HEARTS(String.format("%s\n%s", RED_NINE.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.NINE),
    TEN_OF_HEARTS(String.format("%s\n%s", RED_TEN.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.TEN),
    JACK_OF_HEARTS(String.format("%s\n%s", RED_JACK.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.JACK),
    QUEEN_OF_HEARTS(String.format("%s\n%s", RED_QUEEN.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.QUEEN),
    KING_OF_HEARTS(String.format("%s\n%s", RED_KING.getEmoji(), CustomEmoji.HEARTS.getEmoji()), Suit.HEARTS, Rank.KING),
    ACE_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_ACE.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.ACE),
    TWO_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_TWO.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.TWO),
    THREE_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_THREE.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.THREE),
    FOUR_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_FOUR.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.FOUR),
    FIVE_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_FIVE.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.FIVE),
    SIX_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_SIX.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.SIX),
    SEVEN_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_SEVEN.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.SEVEN),
    EIGHT_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_EIGHT.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.EIGHT),
    NINE_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_NINE.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.NINE),
    TEN_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_TEN.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.TEN),
    JACK_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_JACK.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.JACK),
    QUEEN_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_QUEEN.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.QUEEN),
    KING_OF_SPADES(String.format("%s\n%s", CustomEmoji.BLACK_KING.getEmoji(), CustomEmoji.SPADES.getEmoji()), Suit.SPADES, Rank.KING);

    private final String label;
    private final Suit suit;
    private final Rank rank;

    PlayingCard(String label, Suit suit, Rank rank) {
        this.label = label;
        this.suit = suit;
        this.rank = rank;
    }

    public String getLabel() {
        return label;
    }

    public Rank getRank() {
        return rank;
    }

    public Suit getSuit() {
        return suit;
    }

    public enum Suit {
        CLUBS,
        DIAMONDS,
        HEARTS,
        SPADES
    }

    public enum Rank {
        TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE;

        public int toInt() {
            return ordinal() + 2;
        }
    }
}

