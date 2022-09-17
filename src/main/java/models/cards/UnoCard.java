package models.cards;

import models.CustomEmoji;

import java.awt.*;

public enum UnoCard {

    BLUE_ZERO(0, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_ZERO),
    BLUE_ONE(1, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_ONE),
    BLUE_TWO(2, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_TWO),
    BLUE_THREE(3, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_THREE),
    BLUE_FOUR(4, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_FOUR),
    BLUE_FIVE(5, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_FIVE),
    BLUE_SIX(6, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_SIX),
    BLUE_SEVEN(7, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_SEVEN),
    BLUE_EIGHT(8, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_EIGHT),
    BLUE_NINE(9, Color.BLUE, UnoCardType.NUMBER, CustomEmoji.UNO_BLUE_NINE),
    BLUE_SKIP(-1, Color.BLUE, UnoCardType.SKIP, CustomEmoji.UNO_BLUE_SKIP),
    BLUE_REVERSE(-1, Color.BLUE, UnoCardType.REVERSE, CustomEmoji.UNO_BLUE_REVERSE),
    BLUE_DRAW_TWO(-1, Color.BLUE, UnoCardType.DRAW_TWO, CustomEmoji.UNO_BLUE_DRAW_TWO),

    GREEN_ZERO(0, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_ZERO),
    GREEN_ONE(1, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_ONE),
    GREEN_TWO(2, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_TWO),
    GREEN_THREE(3, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_THREE),
    GREEN_FOUR(4, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_FOUR),
    GREEN_FIVE(5, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_FIVE),
    GREEN_SIX(6, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_SIX),
    GREEN_SEVEN(7, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_SEVEN),
    GREEN_EIGHT(8, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_EIGHT),
    GREEN_NINE(9, Color.GREEN, UnoCardType.NUMBER, CustomEmoji.UNO_GREEN_NINE),
    GREEN_SKIP(-1, Color.GREEN, UnoCardType.SKIP, CustomEmoji.UNO_GREEN_SKIP),
    GREEN_REVERSE(-1, Color.GREEN, UnoCardType.REVERSE, CustomEmoji.UNO_GREEN_REVERSE),
    GREEN_DRAW_TWO(-1, Color.GREEN, UnoCardType.DRAW_TWO, CustomEmoji.UNO_GREEN_DRAW_TWO),

    RED_ZERO(0, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_ZERO),
    RED_ONE(1, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_ONE),
    RED_TWO(2, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_TWO),
    RED_THREE(3, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_THREE),
    RED_FOUR(4, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_FOUR),
    RED_FIVE(5, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_FIVE),
    RED_SIX(6, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_SIX),
    RED_SEVEN(7, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_SEVEN),
    RED_EIGHT(8, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_EIGHT),
    RED_NINE(9, Color.RED, UnoCardType.NUMBER, CustomEmoji.UNO_RED_NINE),
    RED_SKIP(-1, Color.RED, UnoCardType.SKIP, CustomEmoji.UNO_RED_SKIP),
    RED_REVERSE(-1, Color.RED, UnoCardType.REVERSE, CustomEmoji.UNO_RED_REVERSE),
    RED_DRAW_TWO(-1, Color.RED, UnoCardType.DRAW_TWO, CustomEmoji.UNO_RED_DRAW_TWO),

    YELLOW_ZERO(0, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_ZERO),
    YELLOW_ONE(1, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_ONE),
    YELLOW_TWO(2, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_TWO),
    YELLOW_THREE(3, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_THREE),
    YELLOW_FOUR(4, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_FOUR),
    YELLOW_FIVE(5, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_FIVE),
    YELLOW_SIX(6, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_SIX),
    YELLOW_SEVEN(7, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_SEVEN),
    YELLOW_EIGHT(8, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_EIGHT),
    YELLOW_NINE(9, Color.YELLOW, UnoCardType.NUMBER, CustomEmoji.UNO_YELLOW_NINE),
    YELLOW_SKIP(-1, Color.YELLOW, UnoCardType.SKIP, CustomEmoji.UNO_YELLOW_SKIP),
    YELLOW_REVERSE(-1, Color.YELLOW, UnoCardType.REVERSE, CustomEmoji.UNO_YELLOW_REVERSE),
    YELLOW_DRAW_TWO(-1, Color.YELLOW, UnoCardType.DRAW_TWO, CustomEmoji.UNO_YELLOW_DRAW_TWO),

    UNO_WILD(-1, null, UnoCardType.WILD, CustomEmoji.UNO_WILD),
    UNO_WILD_DRAW_FOUR(-1, null, UnoCardType.WILD_DRAW_FOUR, CustomEmoji.UNO_WILD_DRAW_FOUR);

    private final int value;
    private final Color color;
    private final UnoCardType type;
    private final CustomEmoji emoji;

    UnoCard(int value, Color color, UnoCardType type, CustomEmoji emoji) {
        this.value = value;
        this.color = color;
        this.type = type;
        this.emoji = emoji;
    }

    public int getValue() {
        return value;
    }

    public Color getColor() {
        return color;
    }

    public UnoCardType getType() {
        return type;
    }

    public CustomEmoji getEmoji() {
        return emoji;
    }

    public enum UnoCardType {
        NUMBER,
        REVERSE,
        SKIP,
        DRAW_TWO,
        WILD,
        WILD_DRAW_FOUR
    }

}
