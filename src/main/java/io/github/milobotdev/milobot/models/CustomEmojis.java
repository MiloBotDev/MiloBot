package io.github.milobotdev.milobot.models;

import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import org.jetbrains.annotations.NotNull;

/**
 * This enum holds custom emojis for the bot (e.g. for playing cards)
 */
public enum CustomEmojis {

    // playing card emojis
    BLACK_ACE(Emoji.fromCustom("bA", 623575870375985162L, false)),
    BLACK_TWO(Emoji.fromCustom("b2", 623564440574623774L, false)),
    BLACK_THREE(Emoji.fromCustom("b3", 623564440545263626L, false)),
    BLACK_FOUR(Emoji.fromCustom("b4", 623564440624824320L, false)),
    BLACK_FIVE(Emoji.fromCustom("b5", 623564440851316760L, false)),
    BLACK_SIX(Emoji.fromCustom("b6", 623564440679350319L, false)),
    BLACK_SEVEN(Emoji.fromCustom("b7", 623564440754978843L, false)),
    BLACK_EIGHT(Emoji.fromCustom("b8", 623564440826150912L, false)),
    BLACK_NINE(Emoji.fromCustom("b9", 623564440868225025L, false)),
    BLACK_TEN(Emoji.fromCustom("b10", 623564440620630057L, false)),
    BLACK_JACK(Emoji.fromCustom("bJ", 623564440951980084L, false)),
    BLACK_QUEEN(Emoji.fromCustom("bQ", 623564440851185679L, false)),
    BLACK_KING(Emoji.fromCustom("bK", 623564440880807956L, false)),

    RED_ACE(Emoji.fromCustom("rA", 623575868672835584L, false)),
    RED_TWO(Emoji.fromCustom("r2", 623564440989859851L, false)),
    RED_THREE(Emoji.fromCustom("r3", 623564440880545798L, false)),
    RED_FOUR(Emoji.fromCustom("r4", 623564441103106058L, false)),
    RED_FIVE(Emoji.fromCustom("r5", 623564440868225035L, false)),
    RED_SIX(Emoji.fromCustom("r6", 623564440759173121L, false)),
    RED_SEVEN(Emoji.fromCustom("r7", 623564440964694036L, false)),
    RED_EIGHT(Emoji.fromCustom("r8", 623564440901779496L, false)),
    RED_NINE(Emoji.fromCustom("r9", 623564440897454081L, false)),
    RED_TEN(Emoji.fromCustom("r10", 623564440863899663L, false)),
    RED_JACK(Emoji.fromCustom("rJ", 623564440582881282L, false)),
    RED_QUEEN(Emoji.fromCustom("rQ", 623564440880807936L, false)),
    RED_KING(Emoji.fromCustom("rK", 623564441073614848L, false)),

    CLUBS(Emoji.fromCustom("eclubs", 623564441224740866L, false)),
    SPADES(Emoji.fromCustom("espades", 623564441094586378L, false)),
    HEARTS(Emoji.fromCustom("ehearts", 623564441065226267L, false)),
    DIAMONDS(Emoji.fromCustom("ediamonds", 623564440926683148L, false)),

    // uno emojis
    UNO_BLANK(Emoji.fromCustom("BLANK", 999687978236510218L, false)),

    UNO_BLUE_ZERO(Emoji.fromCustom("B0", 999687962331725854L, false)),
    UNO_BLUE_ONE(Emoji.fromCustom("B1", 999687963799736350L, false)),
    UNO_BLUE_TWO(Emoji.fromCustom("B2", 999687964948959353L, false)),
    UNO_BLUE_THREE(Emoji.fromCustom("B3", 999687966047871007L, false)),
    UNO_BLUE_FOUR(Emoji.fromCustom("B4", 999687967465554062L, false)),
    UNO_BLUE_FIVE(Emoji.fromCustom("B5", 999687969013235832L, false)),
    UNO_BLUE_SIX(Emoji.fromCustom("B6", 999687971311726642L, false)),
    UNO_BLUE_SEVEN(Emoji.fromCustom("B7", 999687972532261006L, false)),
    UNO_BLUE_EIGHT(Emoji.fromCustom("B8", 999687973907992576L, false)),
    UNO_BLUE_NINE(Emoji.fromCustom("B9", 999687975434735687L, false)),
    UNO_BLUE_SKIP(Emoji.fromCustom("BS", 999687981147357286L, false)),
    UNO_BLUE_REVERSE(Emoji.fromCustom("BR", 999687979440287847L, false)),
    UNO_BLUE_DRAW_TWO(Emoji.fromCustom("BA2", 999687976852393984L, false)),

    UNO_GREEN_ZERO(Emoji.fromCustom("G0", 999687982653128756L, false)),
    UNO_GREEN_ONE(Emoji.fromCustom("G1", 999687983542321183L, false)),
    UNO_GREEN_TWO(Emoji.fromCustom("G2", 999687985266163864L, false)),
    UNO_GREEN_THREE(Emoji.fromCustom("G3", 999687986520272936L, false)),
    UNO_GREEN_FOUR(Emoji.fromCustom("G4", 999687988097323099L, false)),
    UNO_GREEN_FIVE(Emoji.fromCustom("G5", 999687989343039539L, false)),
    UNO_GREEN_SIX(Emoji.fromCustom("G6", 999687991159160995L, false)),
    UNO_GREEN_SEVEN(Emoji.fromCustom("G7", 999687992786554960L, false)),
    UNO_GREEN_EIGHT(Emoji.fromCustom("G8", 999687994237788161L, false)),
    UNO_GREEN_NINE(Emoji.fromCustom("G9", 999687996687257701L, false)),
    UNO_GREEN_SKIP(Emoji.fromCustom("GS", 999688000810270842L, false)),
    UNO_GREEN_REVERSE(Emoji.fromCustom("GR", 999687999702970449L, false)),
    UNO_GREEN_DRAW_TWO(Emoji.fromCustom("GA2", 999687998310453372L, false)),

    UNO_RED_ZERO(Emoji.fromCustom("R0", 999688003272323102L, false)),
    UNO_RED_ONE(Emoji.fromCustom("R1", 999688004882923701L, false)),
    UNO_RED_TWO(Emoji.fromCustom("R2", 999688006346752121L, false)),
    UNO_RED_THREE(Emoji.fromCustom("R3", 999688007793786970L, false)),
    UNO_RED_FOUR(Emoji.fromCustom("R4", 999688009249214496L, false)),
    UNO_RED_FIVE(Emoji.fromCustom("R5", 999688011426041907L, false)),
    UNO_RED_SIX(Emoji.fromCustom("R6", 999688013078593576L, false)),
    UNO_RED_SEVEN(Emoji.fromCustom("R7", 999688014542418002L, false)),
    UNO_RED_EIGHT(Emoji.fromCustom("R8", 999688016593420328L, false)),
    UNO_RED_NINE(Emoji.fromCustom("R9", 999688018430533663L, false)),
    UNO_RED_SKIP(Emoji.fromCustom("RS", 999688023178481684L, false)),
    UNO_RED_REVERSE(Emoji.fromCustom("RR", 999688021525934230L, false)),
    UNO_RED_DRAW_TWO(Emoji.fromCustom("RA2", 999688020104069251L, false)),

    UNO_YELLOW_ZERO(Emoji.fromCustom("Y0", 999688027372785724L, false)),
    UNO_YELLOW_ONE(Emoji.fromCustom("Y1", 999688028761104547L, false)),
    UNO_YELLOW_TWO(Emoji.fromCustom("Y2", 999688029952299140L, false)),
    UNO_YELLOW_THREE(Emoji.fromCustom("Y3", 999688032225599709L, false)),
    UNO_YELLOW_FOUR(Emoji.fromCustom("Y4", 999688033878163566L, false)),
    UNO_YELLOW_FIVE(Emoji.fromCustom("Y5", 999688035698495489L, false)),
    UNO_YELLOW_SIX(Emoji.fromCustom("Y6", 999688037292318720L, false)),
    UNO_YELLOW_SEVEN(Emoji.fromCustom("Y7", 999688038953259048L, false)),
    UNO_YELLOW_EIGHT(Emoji.fromCustom("Y8", 1020642248989347890L, false)),
    UNO_YELLOW_NINE(Emoji.fromCustom("Y9", 1020642250201518100L, false)),
    UNO_YELLOW_SKIP(Emoji.fromCustom("YS", 1020642253976375387L, false)),
    UNO_YELLOW_REVERSE(Emoji.fromCustom("YR", 1020642252781006888L, false)),
    UNO_YELLOW_DRAW_TWO(Emoji.fromCustom("YA2", 1020642251254267905L, false)),

    UNO_WILD_DRAW_FOUR(Emoji.fromCustom("W4", 999688024583585802L, false)),
    UNO_WILD(Emoji.fromCustom("W", 999688025544081521L, false));

    private final CustomEmoji emoji;

    CustomEmojis(CustomEmoji emoji) {
        this.emoji = emoji;
    }

    /**
     * Returns the discord emoji.
     *
     * @return the discord emoji
     */
    public CustomEmoji getEmoji() {
        return emoji;
    }

    /**
     * Returns the emoji URL.
     *
     * @deprecated use getEmoji().getImageUrl() instead
     *
     * @return the emoji URL
     */
    @Deprecated(forRemoval = true)
    public @NotNull String getCustomEmojiUrl() {
        return emoji.getImageUrl();
    }
}
