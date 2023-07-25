package io.github.milobotdev.milobot.games.uno.model;

import io.github.milobotdev.milobot.models.CustomEmojis;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Optional;

public enum UnoCard {

    BLUE_ZERO(0, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_ZERO, "blue zero", "blue 0", "b0"),
    BLUE_ONE(1, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_ONE, "blue one", "blue 1", "b1"),
    BLUE_TWO(2, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_TWO, "blue two", "blue 2", "b2"),
    BLUE_THREE(3, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_THREE, "blue three", "blue 3", "b3"),
    BLUE_FOUR(4, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_FOUR, "blue four", "blue 4", "b4"),
    BLUE_FIVE(5, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_FIVE, "blue five", "blue 5", "b5"),
    BLUE_SIX(6, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_SIX, "blue six", "blue 6", "b6"),
    BLUE_SEVEN(7, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_SEVEN, "blue seven", "blue 7", "b7"),
    BLUE_EIGHT(8, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_EIGHT, "blue eight", "blue 8", "b8"),
    BLUE_NINE(9, Color.BLUE, UnoCardType.NUMBER, CustomEmojis.UNO_BLUE_NINE, "blue nine", "blue 9", "b9"),
    BLUE_SKIP(-1, Color.BLUE, UnoCardType.SKIP, CustomEmojis.UNO_BLUE_SKIP, "blue skip", "b skip", "bs"),
    BLUE_REVERSE(-1, Color.BLUE, UnoCardType.REVERSE, CustomEmojis.UNO_BLUE_REVERSE, "blue reverse", "b reverse", "br"),
    BLUE_DRAW_TWO(-1, Color.BLUE, UnoCardType.DRAW_TWO, CustomEmojis.UNO_BLUE_DRAW_TWO, "blue draw two", "b draw two", "bdt", "bd2"),

    GREEN_ZERO(0, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_ZERO, "green zero", "green 0", "g0"),
    GREEN_ONE(1, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_ONE, "green one", "green 1", "g1"),
    GREEN_TWO(2, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_TWO, "green two", "green 2", "g2"),
    GREEN_THREE(3, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_THREE, "green three", "green 3", "g3"),
    GREEN_FOUR(4, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_FOUR, "green four", "green 4", "g4"),
    GREEN_FIVE(5, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_FIVE, "green five", "green 5", "g5"),
    GREEN_SIX(6, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_SIX, "green six", "green 6", "g6"),
    GREEN_SEVEN(7, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_SEVEN, "green seven", "green 7", "g7"),
    GREEN_EIGHT(8, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_EIGHT, "green eight", "green 8", "g8"),
    GREEN_NINE(9, Color.GREEN, UnoCardType.NUMBER, CustomEmojis.UNO_GREEN_NINE, "green nine", "green 9", "g9"),
    GREEN_SKIP(-1, Color.GREEN, UnoCardType.SKIP, CustomEmojis.UNO_GREEN_SKIP, "green skip", "g skip", "gs"),
    GREEN_REVERSE(-1, Color.GREEN, UnoCardType.REVERSE, CustomEmojis.UNO_GREEN_REVERSE, "green reverse", "g reverse", "gr"),
    GREEN_DRAW_TWO(-1, Color.GREEN, UnoCardType.DRAW_TWO, CustomEmojis.UNO_GREEN_DRAW_TWO, "green draw two", "g draw two", "gdt", "gd2"),

    RED_ZERO(0, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_ZERO, "red zero", "red 0", "r0"),
    RED_ONE(1, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_ONE, "red one", "red 1", "r1"),
    RED_TWO(2, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_TWO, "red two", "red 2", "r2"),
    RED_THREE(3, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_THREE, "red three", "red 3", "r3"),
    RED_FOUR(4, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_FOUR, "red four", "red 4", "r4"),
    RED_FIVE(5, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_FIVE, "red five", "red 5", "r5"),
    RED_SIX(6, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_SIX, "red six", "red 6", "r6"),
    RED_SEVEN(7, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_SEVEN, "red seven", "red 7", "r7"),
    RED_EIGHT(8, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_EIGHT, "red eight", "red 8", "r8"),
    RED_NINE(9, Color.RED, UnoCardType.NUMBER, CustomEmojis.UNO_RED_NINE, "red nine", "red 9", "r9"),
    RED_SKIP(-1, Color.RED, UnoCardType.SKIP, CustomEmojis.UNO_RED_SKIP, "red skip", "r skip", "rs"),
    RED_REVERSE(-1, Color.RED, UnoCardType.REVERSE, CustomEmojis.UNO_RED_REVERSE, "red reverse", "r reverse", "rr"),
    RED_DRAW_TWO(-1, Color.RED, UnoCardType.DRAW_TWO, CustomEmojis.UNO_RED_DRAW_TWO, "red draw two", "r draw two", "rdt", "rd2"),

    YELLOW_ZERO(0, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_ZERO, "yellow zero", "yellow 0", "y0"),
    YELLOW_ONE(1, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_ONE, "yellow one", "yellow 1", "y1"),
    YELLOW_TWO(2, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_TWO, "yellow two", "yellow 2", "y2"),
    YELLOW_THREE(3, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_THREE, "yellow three", "yellow 3", "y3"),
    YELLOW_FOUR(4, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_FOUR, "yellow four", "yellow 4", "y4"),
    YELLOW_FIVE(5, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_FIVE, "yellow five", "yellow 5", "y5"),
    YELLOW_SIX(6, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_SIX, "yellow six", "yellow 6", "y6"),
    YELLOW_SEVEN(7, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_SEVEN, "yellow seven", "yellow 7", "y7"),
    YELLOW_EIGHT(8, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_EIGHT, "yellow eight", "yellow 8", "y8"),
    YELLOW_NINE(9, Color.YELLOW, UnoCardType.NUMBER, CustomEmojis.UNO_YELLOW_NINE, "yellow nine", "yellow 9", "y9"),
    YELLOW_SKIP(-1, Color.YELLOW, UnoCardType.SKIP, CustomEmojis.UNO_YELLOW_SKIP, "yellow skip", "y skip", "ys"),
    YELLOW_REVERSE(-1, Color.YELLOW, UnoCardType.REVERSE, CustomEmojis.UNO_YELLOW_REVERSE, "yellow reverse", "y reverse", "yr"),
    YELLOW_DRAW_TWO(-1, Color.YELLOW, UnoCardType.DRAW_TWO, CustomEmojis.UNO_YELLOW_DRAW_TWO, "yellow draw two", "y draw two", "ydt", "yd2"),

    UNO_WILD(-1, null, UnoCardType.WILD, CustomEmojis.UNO_WILD, "wild"),
    UNO_WILD_DRAW_FOUR(-1, null, UnoCardType.WILD_DRAW_FOUR, CustomEmojis.UNO_WILD_DRAW_FOUR, "wild draw 4", "wild draw four", "wd4");

    private final int value;
    private final Color color;
    private final UnoCardType type;
    private final CustomEmojis emoji;
    private final String[] names;

    UnoCard(int value, Color color, UnoCardType type, CustomEmojis emoji, String... names) {
        this.value = value;
        this.color = color;
        this.type = type;
        this.emoji = emoji;
        this.names = names;
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

    public CustomEmojis getEmoji() {
        return emoji;
    }

    public String[] getNames() {
        return names;
    }

    public static Optional<UnoCard> getCardByName(String name) {
        for (UnoCard card : values()) {
            for (String cardName : card.getNames()) {
                if (cardName.equalsIgnoreCase(name)) {
                    return Optional.of(card);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Color> getColorByName(@NotNull String name) {
        if(name.equalsIgnoreCase("red")) {
            return Optional.of(Color.RED);
        } else if(name.equalsIgnoreCase("blue")) {
            return Optional.of(Color.BLUE);
        } else if(name.equalsIgnoreCase("green")) {
            return Optional.of(Color.GREEN);
        } else if(name.equalsIgnoreCase("yellow")) {
            return Optional.of(Color.YELLOW);
        }
        return Optional.empty();
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
