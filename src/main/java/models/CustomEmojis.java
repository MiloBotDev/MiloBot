package models;

import net.dv8tion.jda.api.entities.Emoji;

public enum CustomEmojis {

	// playing card emojis
	BLACK_ACE(Emoji.fromEmote("bA", 623575870375985162L, false)),
	BLACK_TWO(Emoji.fromEmote("b2", 623564440574623774L, false)),
	BLACK_THREE(Emoji.fromEmote("b3", 623564440545263626L, false)),
	BLACK_FOUR(Emoji.fromEmote("b4", 623564440624824320L, false)),
	BLACK_FIVE(Emoji.fromEmote("b5", 623564440851316760L, false)),
	BLACK_SIX(Emoji.fromEmote("b6", 623564440679350319L, false)),
	BLACK_SEVEN(Emoji.fromEmote("b7", 623564440754978843L, false)),
	BLACK_EIGHT(Emoji.fromEmote("b8", 623564440826150912L, false)),
	BLACK_NINE(Emoji.fromEmote("b9", 623564440868225025L, false)),
	BLACK_TEN(Emoji.fromEmote("b10", 623564440620630057L, false)),
	BLACK_JACK(Emoji.fromEmote("bJ", 623564440951980084L, false)),
	BLACK_QUEEN(Emoji.fromEmote("bQ", 623564440851185679L, false)),
	BLACK_KING(Emoji.fromEmote("bK", 623564440880807956L, false)),
	RED_ACE(Emoji.fromEmote("rA", 623575868672835584L, false)),
	RED_TWO(Emoji.fromEmote("r2", 623564440989859851L, false)),
	RED_THREE(Emoji.fromEmote("r3", 623564440880545798L, false)),
	RED_FOUR(Emoji.fromEmote("r4", 623564441103106058L, false)),
	RED_FIVE(Emoji.fromEmote("r5", 623564440868225035L, false)),
	RED_SIX(Emoji.fromEmote("r6", 623564440759173121L, false)),
	RED_SEVEN(Emoji.fromEmote("r7", 623564440964694036L, false)),
	RED_EIGHT(Emoji.fromEmote("r8", 623564440901779496L, false)),
	RED_NINE(Emoji.fromEmote("r9", 623564440897454081L, false)),
	RED_TEN(Emoji.fromEmote("r10", 623564440863899663L, false)),
	RED_JACK(Emoji.fromEmote("rJ", 623564440582881282L, false)),
	RED_QUEEN(Emoji.fromEmote("rQ", 623564440880807936L, false)),
	RED_KING(Emoji.fromEmote("rK", 623564441073614848L, false)),
	CLUBS(Emoji.fromEmote("eclubs", 623564441224740866L, false)),
	SPADES(Emoji.fromEmote("espades", 623564441094586378L, false)),
	HEARTS(Emoji.fromEmote("ehearts", 623564441065226267L, false)),
	DIAMONDS(Emoji.fromEmote("ediamonds", 623564440926683148L, false)),

	// uno emojis
	UNO_BLANK(Emoji.fromEmote("BLANK", 999687978236510218L, false)),
	UNO_BLUE_ZERO(Emoji.fromEmote("B0", 999687962331725854L, false)),
	UNO_BLUE_ONE(Emoji.fromEmote("B1", 999687963799736350L, false)),
	UNO_BLUE_TWO(Emoji.fromEmote("B2", 999687964948959353L, false)),
	UNO_BLUE_THREE(Emoji.fromEmote("B3", 99968796604787100L, false)),
	UNO_BLUE_FOUR(Emoji.fromEmote("B4", 999687967465554062L, false)),
	UNO_BLUE_FIVE(Emoji.fromEmote("B5", 999687969013235832L, false)),
	UNO_BLUE_SIX(Emoji.fromEmote("B6", 999687971311726642L, false)),
	UNO_BLUE_SEVEN(Emoji.fromEmote("B7", 999687972532261006L, false)),
	UNO_BLUE_EIGHT(Emoji.fromEmote("B8", 999687973907992576L, false)),
	UNO_BLUE_NINE(Emoji.fromEmote("B9", 999687975434735687L, false)),
	UNO_BLUE_SKIP(Emoji.fromEmote("BS", 999687981147357286L, false)),
	UNO_BLUE_REVERSE(Emoji.fromEmote("BR", 999687979440287847L, false)),
	UNO_BLUE_DRAW_TWO(Emoji.fromEmote("BA2", 999687976852393984L, false)),
	UNO_GREEN_ZERO(Emoji.fromEmote("G0", 999687982653128756L, false)),
	UNO_GREEN_ONE(Emoji.fromEmote("G1", 999687983542321183L, false)),
	UNO_GREEN_TWO(Emoji.fromEmote("G2", 999687985266163864L, false)),
	UNO_GREEN_THREE(Emoji.fromEmote("G3", 999687986520272936L, false)),
	UNO_GREEN_FOUR(Emoji.fromEmote("G4", 999687988097323099L, false)),
	UNO_GREEN_FIVE(Emoji.fromEmote("G5", 999687989343039539L, false)),
	UNO_GREEN_SIX(Emoji.fromEmote("G6", 999687991159160995L, false)),
	UNO_GREEN_SEVEN(Emoji.fromEmote("G7", 999687992786554960L, false));


	private final Emoji emoji;

	CustomEmojis(Emoji emoji) {
		this.emoji = emoji;
	}

	public Emoji getEmoji() {
		return emoji;
	}
}
