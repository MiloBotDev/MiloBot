package models;

import net.dv8tion.jda.api.entities.Emoji;
import org.jetbrains.annotations.NotNull;

public enum CustomEmoji {

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
    UNO_BLUE_THREE(Emoji.fromEmote("B3", 999687966047871007L, false)),
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
    UNO_GREEN_SEVEN(Emoji.fromEmote("G7", 999687992786554960L, false)),
    UNO_GREEN_EIGHT(Emoji.fromEmote("G8", 999687994237788161L, false)),
    UNO_GREEN_NINE(Emoji.fromEmote("G9", 999687996687257701L, false)),
    UNO_GREEN_SKIP(Emoji.fromEmote("GS", 999688000810270842L, false)),
    UNO_GREEN_REVERSE(Emoji.fromEmote("GR", 999687999702970449L, false)),
    UNO_GREEN_DRAW_TWO(Emoji.fromEmote("GA2", 999687998310453372L, false)),

    UNO_RED_ZERO(Emoji.fromEmote("R0", 999688003272323102L, false)),
    UNO_RED_ONE(Emoji.fromEmote("R1", 999688004882923701L, false)),
    UNO_RED_TWO(Emoji.fromEmote("R2", 999688006346752121L, false)),
    UNO_RED_THREE(Emoji.fromEmote("R3", 999688007793786970L, false)),
    UNO_RED_FOUR(Emoji.fromEmote("R4", 999688009249214496L, false)),
    UNO_RED_FIVE(Emoji.fromEmote("R5", 999688011426041907L, false)),
    UNO_RED_SIX(Emoji.fromEmote("R6", 999688013078593576L, false)),
    UNO_RED_SEVEN(Emoji.fromEmote("R7", 999688014542418002L, false)),
    UNO_RED_EIGHT(Emoji.fromEmote("R8", 528389554655199242L, false)),
    UNO_RED_NINE(Emoji.fromEmote("R9", 999688018430533663L, false)),
    UNO_RED_SKIP(Emoji.fromEmote("RS", 999688023178481684L, false)),
    UNO_RED_REVERSE(Emoji.fromEmote("RR", 999688021525934230L, false)),
    UNO_RED_DRAW_TWO(Emoji.fromEmote("RA2", 999688020104069251L, false)),

    UNO_YELLOW_ZERO(Emoji.fromEmote("Y0", 999688027372785724L, false)),
    UNO_YELLOW_ONE(Emoji.fromEmote("Y1", 999688028761104547L, false)),
    UNO_YELLOW_TWO(Emoji.fromEmote("Y2", 999688029952299140L, false)),
    UNO_YELLOW_THREE(Emoji.fromEmote("Y3", 999688032225599709L, false)),
    UNO_YELLOW_FOUR(Emoji.fromEmote("Y4", 999688033878163566L, false)),
    UNO_YELLOW_FIVE(Emoji.fromEmote("Y5", 999688035698495489L, false)),
    UNO_YELLOW_SIX(Emoji.fromEmote("Y6", 999688037292318720L, false)),
    UNO_YELLOW_SEVEN(Emoji.fromEmote("Y7", 999688038953259048L, false)),
    UNO_YELLOW_EIGHT(Emoji.fromEmote("Y8", 1020642248989347890L, false)),
    UNO_YELLOW_NINE(Emoji.fromEmote("Y9", 1020642250201518100L, false)),
    UNO_YELLOW_SKIP(Emoji.fromEmote("YS", 1020642253976375387L, false)),
    UNO_YELLOW_REVERSE(Emoji.fromEmote("YR", 1020642252781006888L, false)),
    UNO_YELLOW_DRAW_TWO(Emoji.fromEmote("YA2", 1020642251254267905L, false)),

    UNO_WILD_DRAW_FOUR(Emoji.fromEmote("W4", 999688024583585802L, false)),
    UNO_WILD(Emoji.fromEmote("W", 999688025544081521L, false));

    private final Emoji emoji;

    CustomEmoji(Emoji emoji) {
        this.emoji = emoji;
    }

    public Emoji getEmoji() {
        return emoji;
    }

    public @NotNull String getCustomEmojiUrl() {
        return "https://cdn.discordapp.com/emojis/" + emoji.getId() + (emoji.isAnimated() ? ".gif" : ".png");
    }
}
