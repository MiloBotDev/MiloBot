package games.uno;

import games.uno.models.UnoCard;
import models.CustomEmoji;
import models.cards.CardDeck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;
import java.util.*;

import static games.uno.models.UnoCard.UNO_WILD;
import static games.uno.models.UnoCard.getColorByName;

public class UnoGame {

    private static final ArrayList<UnoGame> unoGames = new ArrayList<>();
    private final List<User> players = new ArrayList<>();
    private final Map<User, List<UnoCard>> playerData = new HashMap<>();
    private final CardDeck<UnoCard> deck = new CardDeck<>(List.of(UnoCard.values()));
    private final List<UnoCard> playedCards = new ArrayList<>();
    private MessageChannel channel;

    // game specific data
    private UnoCard lastPlayedCard;
    private User playerToMove;
    private int round;
    private int turn;
    private int maxTurns;
    private int amountToGrab;
    private Color currentColour;

    public UnoGame(List<User> players) {
        this.players.addAll(players);
    }

    /**
     * Initializes a game of uno.
     */
    public void setupGame() {
        this.deck.resetDeck();
        this.lastPlayedCard = null;
        // make sure the card on top is a number
        while (this.lastPlayedCard == null) {
            UnoCard unoCard = this.deck.drawCard();
            if (unoCard.getType().equals(UnoCard.UnoCardType.NUMBER)) {
                this.lastPlayedCard = unoCard;
                this.currentColour = unoCard.getColor();
            }
        }
        this.deck.resetDeck();

        this.distributeCards();
        this.round = 1;
        this.turn = 1;
        this.maxTurns = players.size();
        this.amountToGrab = 0;
        this.playerToMove = players.get(0);
        this.currentColour = this.lastPlayedCard.getColor();
    }

    public void start(MessageChannel channel) {
        if (players.size() < 2) {
            throw new IllegalStateException("Not enough players to start a game of uno.");
        } else {
            this.setupGame();
            unoGames.add(this);
            this.players.forEach(user -> user.openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder embedBuilder = generatePlayerCardsEmbed(user);
                privateChannel.sendMessageEmbeds(embedBuilder.build()).queue();
            }));
            this.channel = channel;
            MessageEmbed build = generateStatusEmbed(String.format("A new game has been started. The starting card is %s",
                    lastPlayedCard.getEmoji().getEmoji())).setThumbnail(lastPlayedCard.getEmoji().getCustomEmojiUrl()).build();
            this.channel.sendMessageEmbeds(build).queue();
            this.players.forEach(user -> user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessageEmbeds(build).queue();
            }));
            this.playerData.get(this.players.get(0)).add(UNO_WILD);
        }
    }

    @NotNull
    private EmbedBuilder generatePlayerCardsEmbed(User user) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Your Cards");
        embedBuilder.setColor(Color.BLUE);
        StringBuilder hand = new StringBuilder();
        playerData.get(user).forEach(card -> hand.append(String.format("%s - %s.\n", card.getEmoji().getEmoji(), card.getNames()[0])));
        embedBuilder.setDescription(hand.toString());
        return embedBuilder;
    }

    private @NotNull EmbedBuilder generateStatusEmbed(String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(this.currentColour);
        embed.setDescription(String.format("%s. It is now %s's turn. The current color is %s.",
                message, this.playerToMove.getAsMention(), getColorName(this.currentColour)));
        embed.setFooter(String.format("Round %d, Turn %d/%d", this.round, this.turn, this.maxTurns));
        return embed;
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        List<String> receivedMessage = new ArrayList<>(Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).toList());
        User author = event.getAuthor();
        if (receivedMessage.get(0).startsWith("hand")) {
            author.openPrivateChannel().queue(privateChannel -> privateChannel
                    .sendMessageEmbeds(generatePlayerCardsEmbed(author).build()).queue());
        } else if (receivedMessage.get(0).startsWith("play")) {
            if (!this.playerToMove.equals(author)) {
                event.getChannel().sendMessage("You can only play a card in your own turn.").queue();
                return;
            }
            receivedMessage.remove(0);
            if (receivedMessage.size() > 0) {
                String cardName = String.join(" ", receivedMessage);
                Optional<UnoCard> card;
                if (cardName.startsWith("wild")) {
                    card = Optional.of(UnoCard.UNO_WILD);
                } else {
                    card = UnoCard.getCardByName(cardName);
                }
                if (card.isEmpty()) {
                    event.getChannel().sendMessage("That is not a valid card.").queue();
                } else {
                    List<UnoCard> hand = this.playerData.get(author);
                    UnoCard cardToPlay = card.get();
                    if (hand.contains(cardToPlay)) {
                        if (isValidMove(cardToPlay)) {
                            if (cardToPlay.equals(UnoCard.UNO_WILD)) {
                                // check if the user has specified a colour
                                String[] cardNameArray = cardName.split(" ");
                                String colorName = cardNameArray[cardNameArray.length - 1];
                                Optional<Color> color = getColorByName(colorName);
                                if (color.isEmpty()) {
                                    event.getChannel().sendMessage("That is not a valid color.").queue();
                                    return;
                                } else {
                                    playCard(cardToPlay, color.get(), author);
                                }
                            } else {
                                playCard(cardToPlay, null, author);
                            }
                            CustomEmoji emoji = cardToPlay.getEmoji();
                            EmbedBuilder statusEmbed = generateStatusEmbed(String.format("%s played %s", author.getAsMention(), emoji.getEmoji()));
                            statusEmbed.setThumbnail(emoji.getCustomEmojiUrl());
                            this.channel.sendMessageEmbeds(statusEmbed.build()).queue();
                            players.forEach(user -> user.openPrivateChannel().queue(privateChannel -> {
                                privateChannel.sendMessageEmbeds(statusEmbed.build()).queue();
                            }));
                        } else {
                            event.getChannel().sendMessage("You can't play that card.").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("You do not have that card.").queue();
                    }
                }
            } else {
                event.getChannel().sendMessage("Please specify a card to play.").queue();
            }
        } else if (receivedMessage.get(0).startsWith("draw")) {
            if (!this.playerToMove.equals(author)) {
                event.getChannel().sendMessage("You can only draw a card in your own turn.").queue();
                return;
            }
            drawCard(author);
            nextRound(author);
            CustomEmoji emoji = lastPlayedCard.getEmoji();
            MessageEmbed build = generateStatusEmbed(String.format("%s drew 1 card. The last played card was %s",
                    author.getAsMention(), emoji.getEmoji())).setThumbnail(emoji.getCustomEmojiUrl()).build();
            this.channel.sendMessageEmbeds(build).queue();
            this.players.forEach(user -> user.openPrivateChannel().queue(privateChannel -> {
                privateChannel.sendMessageEmbeds(build).queue();
            }));
        }
    }

    private @Nullable String getColorName(@NotNull Color color) {
        if (color.equals(Color.BLUE)) {
            return "Blue";
        } else if (color.equals(Color.RED)) {
            return "Red";
        } else if (color.equals(Color.GREEN)) {
            return "Green";
        } else if (color.equals(Color.YELLOW)) {
            return "Yellow";
        }
        return null;
    }

    /**
     * Moves on to the next round.
     */
    public void nextRound(User user) {
        this.turn++;
        if (this.turn > maxTurns) {
            this.turn = 1;
            this.round++;
        }
        if (this.playerToMove != null) {
            this.playerToMove = players.get(this.turn - 1);
        }
    }

    /**
     * Draws a variable amount of cards for the player whose turn it currently is.
     *
     * @param amount     Optional int[] amount of cards you want to draw.
     *                   The first number in the int[] is the amount of cars you want to draw.
     * @param userToDraw The user that needs to draw the cards.
     */
    public void drawCard(User userToDraw, Integer... amount) {
        this.playerData.forEach((user, unoCards) -> {
            // find the user that needs to draw a card
            if (userToDraw.getIdLong() == user.getIdLong()) {
                // check if the previously played card is a draw 2 or draw 4 card
                if ((this.lastPlayedCard.getType().equals(UnoCard.UnoCardType.DRAW_TWO) ||
                        this.lastPlayedCard.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) && this.amountToGrab > 0) {
                    innerDraw(user, unoCards, this.amountToGrab);
                    this.amountToGrab = 0;
                }
                innerDraw(user, unoCards, amount);
            }
        });
    }

    private void innerDraw(User user, List<UnoCard> unoCards, Integer @NotNull ... amount) {
        List<Optional<UnoCard>> drawnCards;
        List<UnoCard> cardsToDraw = new ArrayList<>();
        if (amount.length == 0) {
            drawnCards = deck.drawCards(1);
        } else {
            drawnCards = deck.drawCards(amount[0]);
        }
        for (Optional<UnoCard> card : drawnCards) {
            if (card.isEmpty()) {
                // the deck is empty so add the cards on the table to it refresh it
                deck.refreshDeck(playedCards);
                cardsToDraw.add(deck.drawCard());
            } else {
                cardsToDraw.add(card.get());
            }
        }
        // add all cards the players hand
        unoCards.addAll(cardsToDraw);
        user.openPrivateChannel().queue(privateChannel -> {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setColor(Color.BLUE);
            embed.setTitle("You drew the following card(s):");
            StringBuilder hand = new StringBuilder();
            cardsToDraw.forEach(card -> hand.append(String.format("%s - %s.\n", card.getEmoji().getEmoji(), card.getNames()[0])));
            embed.setDescription(hand.toString());
            privateChannel.sendMessageEmbeds(embed.build()).queue();
        });
    }

    /**
     * Hands out 7 cards to each player participating.
     */
    private void distributeCards() {
        for (User participant : this.players) {
            List<Optional<UnoCard>> optionalCards = this.deck.drawCards(7);
            List<UnoCard> hand = new ArrayList<>();
            for (Optional<UnoCard> card : optionalCards) {
                if (card.isEmpty()) {
                    // the deck is empty so add the cards on the table to it refresh it
                    this.deck.refreshDeck(playedCards);
                    hand.add(deck.drawCard());
                } else {
                    hand.add(card.get());
                }
            }
            this.playerData.put(participant, hand);
        }
    }

    /**
     * Checks if the card that's trying to be played is a legal move.
     *
     * @param cardToPlay the card that is being played.
     * @return True if the card can be played.
     */
    public boolean isValidMove(@NotNull UnoCard cardToPlay) {
        boolean isValid = false;
        // the colors are the same so the card can be played
        if (cardToPlay.getColor() != null && cardToPlay.getColor().equals(this.currentColour)) {
            isValid = true;
            // the cards are numerical and have the same value, so they can be played
        } else if (cardToPlay.getValue() != -1 && cardToPlay.getValue() == lastPlayedCard.getValue()) {
            isValid = true;
            // the card is a wild so it can be played
        } else if (cardToPlay.getType().equals(UnoCard.UnoCardType.WILD) || cardToPlay.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
            isValid = true;
            // the card has the same type as the previous, so it can be played (does not work when they card trying to be
            // played is a draw 2 and the previous card was a wild draw 4
        } else if (!cardToPlay.getType().equals(UnoCard.UnoCardType.NUMBER) && cardToPlay.getType().equals(lastPlayedCard.getType())) {
            isValid = true;
            // the card is a draw 2 and the previous card was a wild draw four, so it can be played
        } else if (!cardToPlay.getType().equals(UnoCard.UnoCardType.NUMBER) && cardToPlay.getType().equals(UnoCard.UnoCardType.DRAW_TWO)
                && lastPlayedCard.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
            isValid = true;
        }
        return isValid;
    }

    public void playCard(@NotNull UnoCard cardToPlay, @Nullable Color color, User user) {
        // check if the card can be played
        if (!isValidMove(cardToPlay)) {
            return;
        }
        // remove the cards from the players hand
        this.playerData.get(this.playerToMove).remove(cardToPlay);

        UnoCard.UnoCardType lastPlayedCardType = this.lastPlayedCard.getType();
        UnoCard.UnoCardType cardToPlayType = cardToPlay.getType();

        // check if the last played card is a +2/4
        if (lastPlayedCardType.equals(UnoCard.UnoCardType.DRAW_TWO) || lastPlayedCardType.equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
            // check if the card being played is also a +2/4
            // if yes continue to the next player and increment the cards to draw and skip to the next turn
            if (cardToPlayType.equals(UnoCard.UnoCardType.DRAW_TWO) || cardToPlayType.equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
                if (cardToPlayType.equals(UnoCard.UnoCardType.DRAW_TWO)) {
                    drawTwoCard(cardToPlay, user);
                } else {
                    wildDrawFourCard(cardToPlay, lastPlayedCard, user);
                }
                return;
            } else if (this.amountToGrab > 0) {
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setColor(Color.RED);
                embedBuilder.setDescription(String.format("%s had to draw %d cards.", user.getAsMention(), amountToGrab));
                MessageEmbed build = embedBuilder.build();
                this.channel.sendMessageEmbeds(build).queue();
                this.players.forEach(player -> {
                    player.openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessageEmbeds(build).queue();
                    });
                });
                this.playerData.forEach((participant, unoCards) -> {
                    if (user.getIdLong() == participant.getIdLong()) {
                        innerDraw(participant, unoCards, this.amountToGrab);
                    }
                });
            }
        }
        // the card has the same color
        if ((cardToPlay.getColor() != null && this.currentColour != null)
                && cardToPlay.getColor().equals(this.currentColour)) {
            // check if the card is numerical
            if (cardToPlay.getValue() != -1) {
                this.lastPlayedCard = cardToPlay;
                this.currentColour = cardToPlay.getColor();
                this.nextRound(user);
            } else {
                // the card is not numerical, so It's either
                // a wild a skip or a reverse
                // the card is a skip
                if (cardToPlayType.equals(UnoCard.UnoCardType.SKIP)) {
                    // since the card is a skip we increment the turns twice
                    skipCard(cardToPlay, user);
                    // the card is a draw 2
                } else if (cardToPlayType.equals(UnoCard.UnoCardType.DRAW_TWO)) {
                    drawTwoCard(cardToPlay, user);
                    // the card is a reverse
                } else if (cardToPlayType.equals(UnoCard.UnoCardType.REVERSE)) {
                    reverseCard(cardToPlay, user);
                    // the card is a wild
                } else if (cardToPlayType.equals(UnoCard.UnoCardType.WILD) && color != null) {
                    wildCard(cardToPlay, color, user);
                }
            }
            // the card doesn't have the same color but the same numeric value, so it can be played
        } else if ((cardToPlay.getValue() != -1) && (cardToPlay.getValue() == lastPlayedCard.getValue())) {
            this.lastPlayedCard = cardToPlay;
            this.currentColour = cardToPlay.getColor();
            this.nextRound(user);
            // the card doesn't have the same color, but it is of the same type
        } else if (cardToPlay.getType().equals(lastPlayedCard.getType()) || cardToPlay.getType().equals(UnoCard.UnoCardType.WILD)
                || cardToPlay.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
            if (cardToPlayType.equals(UnoCard.UnoCardType.SKIP)) {
                // since the card is a skip we increment the turns twice
                skipCard(cardToPlay, user);
                // the card is a wild draw 4
            } else if (cardToPlay.equals(UnoCard.UNO_WILD_DRAW_FOUR)) {
                wildDrawFourCard(cardToPlay, lastPlayedCard, user);
                // the card is a reverse
            } else if (cardToPlayType.equals(UnoCard.UnoCardType.REVERSE)) {
                reverseCard(cardToPlay, user);
                // the card is a wild
            } else if (cardToPlayType.equals(UnoCard.UnoCardType.WILD) && color != null) {
                wildCard(cardToPlay, color, user);
            }
        }
        if (this.playerData.get(user).size() == 1) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription(String.format("%s has one card left.", user.getAsMention()));
            embed.setColor(Color.BLUE);
            this.channel.sendMessageEmbeds(embed.build()).queue();
            this.players.forEach(player -> {
                player.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(embed.build()).queue();
                });
            });
        } else if (this.playerData.get(user).size() == 0) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription(String.format("%s has won the game!", user.getAsMention()));
            embed.setColor(Color.GREEN);
            this.channel.sendMessageEmbeds(embed.build()).queue();
            this.players.forEach(player -> {
                player.openPrivateChannel().queue(privateChannel -> {
                    privateChannel.sendMessageEmbeds(embed.build()).queue();
                });
            });
            unoGames.remove(this);
        }
    }

    private void skipCard(@NotNull UnoCard cardToPlay, User user) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = cardToPlay.getColor();
        this.nextRound(user);
        this.nextRound(user);
    }

    private void drawTwoCard(@NotNull UnoCard cardToPlay, User user) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = cardToPlay.getColor();
        this.amountToGrab += 2;
        this.nextRound(user);
    }

    private void wildDrawFourCard(@NotNull UnoCard cardToPlay, @NotNull UnoCard lastPlayedCard, User user) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = lastPlayedCard.getColor();
        this.amountToGrab += 4;
        this.nextRound(user);
    }

    private void reverseCard(@NotNull UnoCard cardToPlay, User user) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = cardToPlay.getColor();
//        Collections.reverse(this.players);
        this.nextRound(user);
    }

    private void wildCard(@NotNull UnoCard cardToPlay, @NotNull Color color, User user) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = color;
        this.nextRound(user);
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            new ArrayList<>(unoGames).forEach(game -> game.onMessage(event));
        }
    }

}
