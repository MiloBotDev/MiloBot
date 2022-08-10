package models.cards;

import models.CustomEmojis;

import static models.CustomEmojis.*;

public enum PlayingCards {

	ACE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_ACE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.ACE),
	TWO_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_TWO.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.TWO),
	THREE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_THREE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.THREE),
	FOUR_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_FOUR.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.FOUR),
	FIVE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_FIVE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.FIVE),
	SIX_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_SIX.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.SIX),
	SEVEN_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_SEVEN.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.SEVEN),
	EIGHT_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_EIGHT.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.EIGHT),
	NINE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_NINE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.NINE),
	TEN_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_TEN.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.TEN),
	JACK_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_JACK.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.JACK),
	QUEEN_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_QUEEN.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.QUEEN),
	KING_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_KING.getEmoji(), CustomEmojis.CLUBS.getEmoji()), Suit.CLUBS, Rank.KING),
	ACE_OF_DIAMONDS(String.format("%s\n%s", RED_ACE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.ACE),
	TWO_OF_DIAMONDS(String.format("%s\n%s", RED_TWO.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.TWO),
	THREE_OF_DIAMONDS(String.format("%s\n%s", RED_THREE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.THREE),
	FOUR_OF_DIAMONDS(String.format("%s\n%s", RED_FOUR.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.FOUR),
	FIVE_OF_DIAMONDS(String.format("%s\n%s", RED_FIVE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.FIVE),
	SIX_OF_DIAMONDS(String.format("%s\n%s", RED_SIX.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.SIX),
	SEVEN_OF_DIAMONDS(String.format("%s\n%s", RED_SEVEN.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.SEVEN),
	EIGHT_OF_DIAMONDS(String.format("%s\n%s", RED_EIGHT.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.EIGHT),
	NINE_OF_DIAMONDS(String.format("%s\n%s", RED_NINE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.NINE),
	TEN_OF_DIAMONDS(String.format("%s\n%s", RED_TEN.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.TEN),
	JACK_OF_DIAMONDS(String.format("%s\n%s", RED_JACK.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.JACK),
	QUEEN_OF_DIAMONDS(String.format("%s\n%s", RED_QUEEN.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.QUEEN),
	KING_OF_DIAMONDS(String.format("%s\n%s", RED_KING.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), Suit.DIAMONDS, Rank.KING),
	ACE_OF_HEARTS(String.format("%s\n%s", RED_ACE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.ACE),
	TWO_OF_HEARTS(String.format("%s\n%s", RED_TWO.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.TWO),
	THREE_OF_HEARTS(String.format("%s\n%s", RED_THREE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.THREE),
	FOUR_OF_HEARTS(String.format("%s\n%s", RED_FOUR.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.FOUR),
	FIVE_OF_HEARTS(String.format("%s\n%s", RED_FIVE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.FIVE),
	SIX_OF_HEARTS(String.format("%s\n%s", RED_SIX.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.SIX),
	SEVEN_OF_HEARTS(String.format("%s\n%s", RED_SEVEN.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.SEVEN),
	EIGHT_OF_HEARTS(String.format("%s\n%s", RED_EIGHT.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.EIGHT),
	NINE_OF_HEARTS(String.format("%s\n%s", RED_NINE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.NINE),
	TEN_OF_HEARTS(String.format("%s\n%s", RED_TEN.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.TEN),
	JACK_OF_HEARTS(String.format("%s\n%s", RED_JACK.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.JACK),
	QUEEN_OF_HEARTS(String.format("%s\n%s", RED_QUEEN.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.QUEEN),
	KING_OF_HEARTS(String.format("%s\n%s", RED_KING.getEmoji(), CustomEmojis.HEARTS.getEmoji()), Suit.HEARTS, Rank.KING),
	ACE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_ACE.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.ACE),
	TWO_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_TWO.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.TWO),
	THREE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_THREE.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.THREE),
	FOUR_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_FOUR.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.FOUR),
	FIVE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_FIVE.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.FIVE),
	SIX_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_SIX.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.SIX),
	SEVEN_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_SEVEN.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.SEVEN),
	EIGHT_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_EIGHT.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.EIGHT),
	NINE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_NINE.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.NINE),
	TEN_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_TEN.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.TEN),
	JACK_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_JACK.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.JACK),
	QUEEN_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_QUEEN.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.QUEEN),
	KING_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_KING.getEmoji(), CustomEmojis.SPADES.getEmoji()), Suit.SPADES, Rank.KING);


	private final String label;
	private final Suit suit;
	private final Rank rank;

	PlayingCards(String label, Suit suit, Rank rank) {
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

