package models;

import static models.CustomEmojis.*;

public enum PlayingCards {

	ACE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_ACE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 11),
	TWO_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_TWO.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 2),
	THREE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_THREE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 3),
	FOUR_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_FOUR.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 4),
	FIVE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_FIVE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 5),
	SIX_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_SIX.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 6),
	SEVEN_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_SEVEN.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 7),
	EIGHT_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_EIGHT.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 8),
	NINE_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_NINE.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 9),
	TEN_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_TEN.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 10),
	JACK_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_JACK.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 10),
	QUEEN_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_QUEEN.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 10),
	KING_OF_CLUBS(String.format("%s\n%s", CustomEmojis.BLACK_KING.getEmoji(), CustomEmojis.CLUBS.getEmoji()), 10),
	ACE_OF_DIAMONDS(String.format("%s\n%s", RED_ACE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 11),
	TWO_OF_DIAMONDS(String.format("%s\n%s", RED_TWO.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 2),
	THREE_OF_DIAMONDS(String.format("%s\n%s", RED_THREE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 3),
	FOUR_OF_DIAMONDS(String.format("%s\n%s", RED_FOUR.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 4),
	FIVE_OF_DIAMONDS(String.format("%s\n%s", RED_FIVE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 5),
	SIX_OF_DIAMONDS(String.format("%s\n%s", RED_SIX.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 6),
	SEVEN_OF_DIAMONDS(String.format("%s\n%s", RED_SEVEN.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 7),
	EIGHT_OF_DIAMONDS(String.format("%s\n%s", RED_EIGHT.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 8),
	NINE_OF_DIAMONDS(String.format("%s\n%s", RED_NINE.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 9),
	TEN_OF_DIAMONDS(String.format("%s\n%s", RED_TEN.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 10),
	JACK_OF_DIAMONDS(String.format("%s\n%s", RED_JACK.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 10),
	QUEEN_OF_DIAMONDS(String.format("%s\n%s", RED_QUEEN.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 10),
	KING_OF_DIAMONDS(String.format("%s\n%s", RED_KING.getEmoji(), CustomEmojis.DIAMONDS.getEmoji()), 10),
	ACE_OF_HEARTS(String.format("%s\n%s", RED_ACE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 11),
	TWO_OF_HEARTS(String.format("%s\n%s", RED_TWO.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 2),
	THREE_OF_HEARTS(String.format("%s\n%s", RED_THREE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 3),
	FOUR_OF_HEARTS(String.format("%s\n%s", RED_FOUR.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 4),
	FIVE_OF_HEARTS(String.format("%s\n%s", RED_FIVE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 5),
	SIX_OF_HEARTS(String.format("%s\n%s", RED_SIX.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 6),
	SEVEN_OF_HEARTS(String.format("%s\n%s", RED_SEVEN.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 7),
	EIGHT_OF_HEARTS(String.format("%s\n%s", RED_EIGHT.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 8),
	NINE_OF_HEARTS(String.format("%s\n%s", RED_NINE.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 9),
	TEN_OF_HEARTS(String.format("%s\n%s", RED_TEN.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 10),
	JACK_OF_HEARTS(String.format("%s\n%s", RED_JACK.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 10),
	QUEEN_OF_HEARTS(String.format("%s\n%s", RED_QUEEN.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 10),
	KING_OF_HEARTS(String.format("%s\n%s", RED_KING.getEmoji(), CustomEmojis.HEARTS.getEmoji()), 10),
	ACE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_ACE.getEmoji(), CustomEmojis.SPADES.getEmoji()), 11),
	TWO_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_TWO.getEmoji(), CustomEmojis.SPADES.getEmoji()), 2),
	THREE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_THREE.getEmoji(), CustomEmojis.SPADES.getEmoji()), 3),
	FOUR_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_FOUR.getEmoji(), CustomEmojis.SPADES.getEmoji()), 4),
	FIVE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_FIVE.getEmoji(), CustomEmojis.SPADES.getEmoji()), 5),
	SIX_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_SIX.getEmoji(), CustomEmojis.SPADES.getEmoji()), 6),
	SEVEN_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_SEVEN.getEmoji(), CustomEmojis.SPADES.getEmoji()), 7),
	EIGHT_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_EIGHT.getEmoji(), CustomEmojis.SPADES.getEmoji()), 8),
	NINE_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_NINE.getEmoji(), CustomEmojis.SPADES.getEmoji()), 9),
	TEN_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_TEN.getEmoji(), CustomEmojis.SPADES.getEmoji()), 10),
	JACK_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_JACK.getEmoji(), CustomEmojis.SPADES.getEmoji()), 10),
	QUEEN_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_QUEEN.getEmoji(), CustomEmojis.SPADES.getEmoji()), 10),
	KING_OF_SPADES(String.format("%s\n%s", CustomEmojis.BLACK_KING.getEmoji(), CustomEmojis.SPADES.getEmoji()), 10);


	private final String label;
	private final int value;

	PlayingCards(String label, int value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public int getValue() {
		return value;
	}
}

