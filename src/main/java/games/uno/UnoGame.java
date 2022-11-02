package games.uno;

import database.dao.UnoDao;
import games.hungergames.models.LobbyEntry;
import models.CustomEmoji;
import models.cards.CardDeck;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.observer.ObservableList;
import utility.observer.Observer;
import utility.TimeTracker;

import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static games.uno.UnoCard.getColorByName;

public class UnoGame {

    private static final int BOT_MOVE_DELAY = 2;
    private static final int TURN_TIME_LIMIT = 60;
    private static final int TIMEOUT_CLEANUP_FREQUENCY = 5;

    private static final Logger logger = LoggerFactory.getLogger(UnoGame.class);

    private static final ObservableList<UnoGame> unoGames = new ObservableList<>();
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean schedulerOn = false;
    private static ScheduledFuture<?> future = scheduler.scheduleWithFixedDelay(new TimeOutCleanup(),
            TIMEOUT_CLEANUP_FREQUENCY, TIMEOUT_CLEANUP_FREQUENCY, TimeUnit.SECONDS);
    private static final class TimeOutCleanup implements Runnable {
        @Override
        public void run() {
            for (UnoGame unoGame : unoGames) {
                unoGame.checkTurnTime();
            }
        }
    }
    private static final class UnoGameObserver implements Observer {
        @Override
        public void update() {
            if (unoGames.size() == 0) {
                if(schedulerOn) {
                    future.cancel(true);
                    schedulerOn = false;
                }
            } else {
                if(!schedulerOn) {
                    future = scheduler.scheduleWithFixedDelay(new TimeOutCleanup(), TIMEOUT_CLEANUP_FREQUENCY,
                            TIMEOUT_CLEANUP_FREQUENCY, TimeUnit.SECONDS);
                    schedulerOn = true;
                }
            }
        }
    }

    private final List<LobbyEntry> players = new ArrayList<>();
    private final Map<LobbyEntry, List<UnoCard>> playerData = new HashMap<>();
    private final CardDeck<UnoCard> deck = new CardDeck<>(List.of(UnoCard.values()));
    private final List<UnoCard> playedCards = new ArrayList<>();
    private final TimeTracker gameTimeTracker = new TimeTracker();
    private final TimeTracker turnTimeTracker = new TimeTracker();
    private final UnoDao unoDao = UnoDao.getInstance();
    private MessageChannel channel;
    private boolean awaitingResponse;

    // game specific data
    private UnoCard lastPlayedCard;
    private LobbyEntry playerToMove;
    private int turn;
    private int maxTurns;
    private int amountToGrab;
    private Color currentColour;
    private boolean isReversed;
    private int totalCardsPlayed;
    private int totalTurnsPassed;
    private int totalCardsDrawn;
    private boolean gameEnded;

    static {
        unoGames.addObserver(new UnoGameObserver());
    }

    public UnoGame(List<LobbyEntry> players) {
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
            UnoCard unoCard = this.deck.drawCard().get();
            if (unoCard.getType().equals(UnoCard.UnoCardType.NUMBER)) {
                this.lastPlayedCard = unoCard;
                this.currentColour = unoCard.getColor();
            }
        }

        this.deck.resetDeck();
        this.deck.removeCard(this.lastPlayedCard);
        this.playedCards.add(this.lastPlayedCard);
        try {
            this.distributeCards();
        } catch (IllegalStateException e) {
            logger.error("Error while distributing cards", e);
        }
        this.turn = 1;
        this.maxTurns = players.size();
        this.amountToGrab = 0;
        this.playerToMove = players.get(0);
        this.currentColour = this.lastPlayedCard.getColor();
        this.isReversed = false;
        this.totalCardsPlayed = 0;
        this.totalTurnsPassed = 0;
        this.gameEnded = false;
        this.awaitingResponse = false;
        this.gameTimeTracker.reset();
        this.turnTimeTracker.start();
    }

    public void start(MessageChannel channel) {
        if (players.size() < 2) {
            throw new IllegalStateException("Not enough players to start a game of uno.");
        } else {
            this.setupGame();
            unoGames.add(this);
            channel.sendMessage(this.players.toString()).queue();
            this.players.forEach(lobbyEntry -> {
                if (!lobbyEntry.isBot()) {
                    User user = lobbyEntry.getUser();
                    user.openPrivateChannel().queue(privateChannel -> {
                        EmbedBuilder embedBuilder = generatePlayerCardsEmbed(lobbyEntry);
                        privateChannel.sendMessageEmbeds(embedBuilder.build()).queue();
                    });
                }
            });
            this.channel = channel;
            MessageEmbed build = generateStatusEmbed(String.format("A new game has been started. The starting card is %s",
                    lastPlayedCard.getEmoji().getEmoji())).setThumbnail(lastPlayedCard.getEmoji().getCustomEmojiUrl()).build();
            sendEmbedToPlayers(build);
        }
        this.gameTimeTracker.start();
        checkForBotMove();
    }

    @NotNull
    private EmbedBuilder generatePlayerCardsEmbed(LobbyEntry lobbyEntry) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setTitle("Your Cards");
        embedBuilder.setColor(Color.BLUE);
        StringBuilder hand = new StringBuilder();
        playerData.get(lobbyEntry).forEach(card -> hand.append(String.format("%s - %s.\n", card.getEmoji().getEmoji(), card.getNames()[0])));
        embedBuilder.setDescription(hand.toString());
        return embedBuilder;
    }

    private @NotNull EmbedBuilder generateStatusEmbed(String message) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(this.currentColour);
        if (gameEnded) {
            embed.setDescription(message);
        } else {
            embed.setDescription(String.format("%s\n\n It is now %s's turn.\n\n The current color is %s.",
                    message, this.playerToMove.getMention(), getColorName(this.currentColour)));
        }
        embed.setFooter(String.format("Total cards played: %d. Total turns passed: %d. Total cards drawn: %d. " +
                        "Total time passed: %d seconds.", this.totalCardsPlayed, this.totalTurnsPassed, this.totalCardsDrawn,
                this.gameTimeTracker.getElapsedTimeSecs()));
        return embed;
    }

    private @NotNull EmbedBuilder generateWinnerEmbed() {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(this.currentColour);
        embed.setDescription(String.format("The winner is %s!\nThis game lasted %d seconds.",
                this.playerToMove.getMention(), this.gameTimeTracker.getElapsedTimeSecs()));
        embed.setFooter(String.format("Total cards played: %d. Total turns passed: %d.", this.totalCardsPlayed, this.totalTurnsPassed));
        return embed;
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        if (this.gameEnded) {
            return;
        }
        List<String> receivedMessage = new ArrayList<>(Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).toList());
        User author = event.getAuthor();
        LobbyEntry authorEntry = players.stream().filter(lobbyEntry -> {
            if (lobbyEntry.getUser() == null) {
                return false;
            }
            return lobbyEntry.getUser().equals(author);
        }).findFirst().orElse(null);
        if (receivedMessage.get(0).startsWith("hand")) {
            players.forEach(lobbyEntry -> {
                if (lobbyEntry.isBot()) {
                    return;
                }
                if (lobbyEntry.getUser().equals(author)) {
                    EmbedBuilder embedBuilder = generatePlayerCardsEmbed(lobbyEntry);
                    lobbyEntry.getUser().openPrivateChannel().queue(privateChannel -> privateChannel.sendMessageEmbeds(embedBuilder.build()).queue());
                }
            });
        } else if (receivedMessage.get(0).startsWith("play")) {
            if (this.playerToMove.getUser() == null || !this.playerToMove.getUser().equals(author)) {
                event.getChannel().sendMessage(String.format("You can only play a card in your own turn. Its currently %s's turn.",
                        this.playerToMove.getMention())).queue();
                return;
            }
            receivedMessage.remove(0);
            if (receivedMessage.size() > 0) {
                String cardName = String.join(" ", receivedMessage);
                Optional<UnoCard> card;
                if (cardName.startsWith("wild") && (!cardName.contains("4") || !cardName.contains("four"))) {
                    card = Optional.of(UnoCard.UNO_WILD);
                } else {
                    card = UnoCard.getCardByName(cardName);
                }
                if (card.isEmpty()) {
                    event.getChannel().sendMessage("That is not a valid card.").queue();
                } else {
                    List<UnoCard> hand = this.playerData.get(this.playerToMove);
                    UnoCard cardToPlay = card.get();
                    if (hand.contains(cardToPlay)) {
                        if (isValidMove(cardToPlay)) {
                            if (hand.size() == 1 && (cardToPlay.getType().equals(UnoCard.UnoCardType.DRAW_TWO) ||
                                    cardToPlay.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR) ||
                                    cardToPlay.getType().equals(UnoCard.UnoCardType.SKIP) ||
                                    cardToPlay.getType().equals(UnoCard.UnoCardType.REVERSE) ||
                                    cardToPlay.getType().equals(UnoCard.UnoCardType.WILD))) {
                                event.getChannel().sendMessage("You cannot play this card as your last card.").queue();
                                return;
                            }
                            if (cardToPlay.equals(UnoCard.UNO_WILD)) {
                                // check if the user has specified a colour
                                String[] cardNameArray = cardName.split(" ");
                                String colorName = cardNameArray[cardNameArray.length - 1];
                                Optional<Color> color = getColorByName(colorName);
                                if (color.isEmpty()) {
                                    event.getChannel().sendMessage("That is not a valid color.").queue();
                                    return;
                                } else {
                                    playCard(cardToPlay, color.get(), authorEntry);
                                }
                            } else {
                                playCard(cardToPlay, null, authorEntry);
                            }
                            this.totalCardsPlayed++;
                            CustomEmoji emoji = cardToPlay.getEmoji();
                            EmbedBuilder statusEmbed = generateStatusEmbed(String.format("%s played %s", author.getAsMention(), emoji.getEmoji()));
                            generateCardPlayedEmbed(emoji, statusEmbed);
                            checkForBotMove();
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
            if (this.playerToMove.getUser() == null || !this.playerToMove.getUser().equals(author)) {
                event.getChannel().sendMessage(String.format("You can only draw a card in your own turn. Its currently %s's turn.",
                        this.playerToMove.getMention())).queue();
                return;
            }
            try {
                drawCard(authorEntry);
            } catch (IllegalStateException e) {
                event.getChannel().sendMessage("You cannot draw a card because there are no cards left to draw." +
                        " Do you want to skip your turn? Type Y or N.").queue();
                this.awaitingResponse = true;
                return;
            }
            this.nextRound();
            CustomEmoji emoji = lastPlayedCard.getEmoji();
            MessageEmbed build = generateStatusEmbed(String.format("%s drew 1 card.\n\n The last played card was %s",
                    author.getAsMention(), lastPlayedCard.getEmoji().getEmoji())).setThumbnail(emoji.getCustomEmojiUrl()).build();
            sendEmbedToPlayers(build);
            checkForBotMove();
        } else if (receivedMessage.get(0).toLowerCase().startsWith("y") && this.awaitingResponse) {
            if (this.playerToMove.getUser() == null || !this.playerToMove.getUser().equals(author)) {
                return;
            }
            this.awaitingResponse = false;
            this.nextRound();
            CustomEmoji emoji = lastPlayedCard.getEmoji();
            EmbedBuilder build = generateStatusEmbed(String.format("%s skipped their turn.\n\n The last played card was %s",
                    author.getAsMention(), emoji.getEmoji()));
            sendEmbedToPlayers(build.setThumbnail(emoji.getCustomEmojiUrl()).build());
            checkForBotMove();
        } else if (receivedMessage.get(0).toLowerCase().startsWith("n") && this.awaitingResponse) {
            if (this.playerToMove.getUser() == null || !this.playerToMove.getUser().equals(author)) {
                return;
            }
            this.awaitingResponse = false;
        }
    }

    private void sendEmbedToPlayers(MessageEmbed build) {
        this.channel.sendMessageEmbeds(build).queue();
        this.players.forEach(lobbyEntry -> {
            if (!lobbyEntry.isBot()) {
                User user = lobbyEntry.getUser();
                user.openPrivateChannel().queue(privateChannel -> privateChannel
                        .sendMessageEmbeds(build).queue());
            }
        });
    }

    private void checkForBotMove() {
        if (this.playerToMove.isBot()) {
            Executors.newScheduledThreadPool(1).schedule(this::botMove, BOT_MOVE_DELAY, TimeUnit.SECONDS);
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

    private Color getRandomColor() {
        List<Color> colors = new ArrayList<>(Arrays.asList(Color.BLUE, Color.RED, Color.GREEN, Color.YELLOW));
        Collections.shuffle(colors);
        return colors.get(0);
    }

    /**
     * Moves on to the next round.
     */
    public void nextRound() {
        this.turnTimeTracker.reset();
        this.awaitingResponse = false;
        if (this.playerData.get(this.playerToMove).size() == 0) {
            this.endGame();
            return;
        }
        if (this.isReversed) {
            this.turn--;
            if (this.turn < 1) {
                this.turn = this.maxTurns;
            }
        } else {
            this.turn++;
            if (this.turn > this.maxTurns) {
                this.turn = 1;
            }
        }
        this.totalTurnsPassed++;
        this.playerToMove = players.get(this.turn - 1);
        this.turnTimeTracker.start();
    }

    private void checkTurnTime() {
        if (this.turnTimeTracker.getElapsedTimeSecs() >= TURN_TIME_LIMIT) {
            this.turnTimeTracker.reset();
            LobbyEntry removedPlayer = this.playerToMove;
            this.maxTurns--;
            if(this.isReversed) {
                if (this.turn < 1) {
                    this.turn = this.maxTurns;
                }
            } else {
                if (this.turn > this.maxTurns) {
                    this.turn = 1;
                }
            }
            this.nextRound();
            CustomEmoji emoji = this.lastPlayedCard.getEmoji();
            sendEmbedToPlayers(generateStatusEmbed(String.format("%s took too long to play their turn. They have been skipped.",
                    removedPlayer.getMention())).setThumbnail(emoji.getCustomEmojiUrl()).build());
            this.playerData.remove(removedPlayer);
            this.players.remove(removedPlayer);
            if (this.players.size() == 1) {
                this.endGame();
            }
            checkForBotMove();
        }
    }

    private void endGame() {
        gameTimeTracker.stop();
        this.channel.sendMessageEmbeds(generateWinnerEmbed().build()).queueAfter(1, TimeUnit.SECONDS);
        this.players.forEach(lobbyEntry -> {
            if (!lobbyEntry.isBot()) {
                User user = lobbyEntry.getUser();
                user.openPrivateChannel().queue(privateChannel -> privateChannel
                        .sendMessageEmbeds(generateWinnerEmbed().build()).queueAfter(1, TimeUnit.SECONDS));
            }
        });
        this.gameEnded = true;
        unoGames.remove(this);
    }

    private void botMove() {
        if (this.gameEnded) {
            return;
        }
        LobbyEntry bot = this.playerToMove;
        List<UnoCard> botHand = this.playerData.get(bot);
        UnoCard cardToPlay = null;
        if (((lastPlayedCard.getType().equals(UnoCard.UnoCardType.DRAW_TWO) ||
                lastPlayedCard.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) && botHand.size() > 1)) {
            for (UnoCard card : botHand) {
                if (card.getType().equals(UnoCard.UnoCardType.DRAW_TWO) || card.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
                    cardToPlay = card;
                    break;
                }
            }
            if (cardToPlay == null) {
                for (UnoCard card : botHand) {
                    if (isValidMove(card) &&
                            ((botHand.size() == 1 && card.getType().equals(UnoCard.UnoCardType.NUMBER)) || botHand.size() > 1)) {
                        cardToPlay = card;
                        break;
                    }
                }
            }
        } else {
            for (UnoCard card : botHand) {
                if (isValidMove(card) &&
                        ((botHand.size() == 1 && card.getType().equals(UnoCard.UnoCardType.NUMBER)) || botHand.size() > 1)) {
                    cardToPlay = card;
                    break;
                }
            }
        }
        if (cardToPlay == null) {
            try {
                drawCard(bot);
            } catch (IllegalStateException e) {
                this.nextRound();
                CustomEmoji emoji = lastPlayedCard.getEmoji();
                EmbedBuilder build = generateStatusEmbed(String.format("%s skipped their turn.\n\n The last played card was %s",
                        bot.getMention(), emoji.getEmoji()));
                sendEmbedToPlayers(build.setThumbnail(emoji.getCustomEmojiUrl()).build());
                return;
            }

            this.nextRound();
            CustomEmoji emoji = lastPlayedCard.getEmoji();
            MessageEmbed build = generateStatusEmbed(String.format("%s drew 1 card.\n\n The last played card was %s",
                    bot.getMention(), emoji.getEmoji())).setThumbnail(lastPlayedCard.getEmoji().getCustomEmojiUrl()).build();
            sendEmbedToPlayers(build);
        } else {
            if (cardToPlay.equals(UnoCard.UNO_WILD)) {
                // find the most common color in the bots hand
                playCard(cardToPlay, getRandomColor(), bot);
            } else {
                playCard(cardToPlay, null, bot);
            }
            this.totalCardsPlayed++;
            CustomEmoji emoji = cardToPlay.getEmoji();
            EmbedBuilder statusEmbed = generateStatusEmbed(String.format("%s played %s", bot.getMention(), emoji.getEmoji()));
            generateCardPlayedEmbed(emoji, statusEmbed);
        }
        checkForBotMove();
    }


    private void generateCardPlayedEmbed(@NotNull CustomEmoji emoji, @NotNull EmbedBuilder statusEmbed) {
        statusEmbed.setThumbnail(emoji.getCustomEmojiUrl());
        this.channel.sendMessageEmbeds(statusEmbed.build()).queue();
        players.forEach(lobbyEntry -> {
            if (!lobbyEntry.isBot()) {
                User user = lobbyEntry.getUser();
                user.openPrivateChannel().queue(privateChannel -> privateChannel
                        .sendMessageEmbeds(statusEmbed.build()).queue());
            }
        });
    }

    /**
     * Draws a variable amount of cards for the player whose turn it currently is.
     *
     * @param amount     Optional int[] amount of cards you want to draw.
     *                   The first number in the int[] is the amount of cars you want to draw.
     * @param userToDraw The user that needs to draw the cards.
     */
    public void drawCard(LobbyEntry userToDraw, Integer... amount) throws IllegalStateException {
        this.playerData.forEach((lobbyEntry, unoCards) -> {
            // find the user that needs to draw a card
            if (userToDraw.getUserId() == lobbyEntry.getUserId() || Objects.equals(userToDraw.getUserName(), lobbyEntry.getUserName())) {
                // check if the previously played card is a draw 2 or draw 4 card
                if ((this.lastPlayedCard.getType().equals(UnoCard.UnoCardType.DRAW_TWO) ||
                        this.lastPlayedCard.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) && this.amountToGrab > 0) {
                    innerDraw(lobbyEntry, unoCards, this.amountToGrab);
                    generateHadToDrawEmbed(lobbyEntry);
                    this.amountToGrab = 0;
                }
                innerDraw(lobbyEntry, unoCards, amount);
            }
        });
    }

    private void generateHadToDrawEmbed(@NotNull LobbyEntry lobbyEntry) {
        EmbedBuilder embedBuilder = new EmbedBuilder();
        embedBuilder.setColor(Color.RED);
        embedBuilder.setDescription(String.format("%s had to draw %d cards.", lobbyEntry.getMention(), amountToGrab));
        MessageEmbed build = embedBuilder.build();
        sendEmbedToPlayers(build);
    }

    private void innerDraw(LobbyEntry user, List<UnoCard> unoCards, Integer @NotNull ... amount) throws IllegalStateException {
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
                deck.refresh(playedCards);
                playedCards.clear();
                Optional<UnoCard> unoCard = deck.drawCard();
                if (unoCard.isEmpty()) {
                    this.amountToGrab = 0;
                    throw new IllegalStateException("The deck is empty and could not be refreshed.");
                } else {
                    cardsToDraw.add(unoCard.get());
                }
            } else {
                cardsToDraw.add(card.get());
            }
        }
        // add all cards the players hand
        unoCards.addAll(cardsToDraw);
        this.totalCardsDrawn += cardsToDraw.size();
        if (!user.isBot()) {
            user.getUser().openPrivateChannel().queue(privateChannel -> {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(Color.BLUE);
                embed.setTitle("You drew the following card(s):");
                StringBuilder hand = new StringBuilder();
                cardsToDraw.forEach(card -> hand.append(String.format("%s - %s.\n", card.getEmoji().getEmoji(), card.getNames()[0])));
                embed.setDescription(hand.toString());
                privateChannel.sendMessageEmbeds(embed.build()).queue();
            });
        }

    }

    /**
     * Hands out 7 cards to each player participating.
     */
    private void distributeCards() throws IllegalStateException {
        for (LobbyEntry participant : this.players) {
            List<Optional<UnoCard>> optionalCards = this.deck.drawCards(7);
            List<UnoCard> hand = new ArrayList<>();
            for (Optional<UnoCard> card : optionalCards) {
                if (card.isEmpty()) {
                    // the deck is empty so add the cards on the table to it refresh it
                    this.deck.refresh(playedCards);
                    this.playedCards.clear();
                    Optional<UnoCard> unoCard = deck.drawCard();
                    if (unoCard.isEmpty()) {
                        throw new IllegalStateException("The deck is empty and there are no cards to fill it with.");
                    } else {
                        hand.add(unoCard.get());
                    }
                    this.playedCards.clear();
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

    public void playCard(@NotNull UnoCard cardToPlay, @Nullable Color color, LobbyEntry user) {
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
                    drawTwoCard(cardToPlay);
                } else {
                    wildDrawFourCard(cardToPlay, lastPlayedCard);
                }
                checkCardsLeft(user);
                return;
            } else if (this.amountToGrab > 0) {
                generateHadToDrawEmbed(user);
                this.playerData.forEach((participant, unoCards) -> {
                    if ((user.getUser() != null && user.getUser().getIdLong() == participant.getUserId()) || user.getUserName().equals(participant.getUserName())) {
                        innerDraw(participant, unoCards, this.amountToGrab);
                    }
                });
                this.amountToGrab = 0;
            }
        }
        // the card has the same color
        if ((cardToPlay.getColor() != null && this.currentColour != null)
                && cardToPlay.getColor().equals(this.currentColour)) {
            // check if the card is numerical
            if (cardToPlay.getValue() != -1) {
                this.lastPlayedCard = cardToPlay;
                this.currentColour = cardToPlay.getColor();
                this.playedCards.add(cardToPlay);
                this.nextRound();
            } else {
                // the card is not numerical, so It's either
                // a wild a skip or a reverse
                // the card is a skip
                if (cardToPlayType.equals(UnoCard.UnoCardType.SKIP)) {
                    // since the card is a skip we increment the turns twice
                    skipCard(cardToPlay);
                    // the card is a draw 2
                } else if (cardToPlayType.equals(UnoCard.UnoCardType.DRAW_TWO)) {
                    drawTwoCard(cardToPlay);
                    // the card is a reverse
                } else if (cardToPlayType.equals(UnoCard.UnoCardType.REVERSE)) {
                    reverseCard(cardToPlay);
                    // the card is a wild
                } else if (cardToPlayType.equals(UnoCard.UnoCardType.WILD) && color != null) {
                    wildCard(cardToPlay, color);
                }
            }
            // the card doesn't have the same color but the same numeric value, so it can be played
        } else if ((cardToPlay.getValue() != -1) && (cardToPlay.getValue() == lastPlayedCard.getValue())) {
            this.lastPlayedCard = cardToPlay;
            this.currentColour = cardToPlay.getColor();
            this.playedCards.add(cardToPlay);
            this.nextRound();
            // the card doesn't have the same color, but it is of the same type
        } else if (cardToPlay.getType().equals(lastPlayedCard.getType()) || cardToPlay.getType().equals(UnoCard.UnoCardType.WILD)
                || cardToPlay.getType().equals(UnoCard.UnoCardType.WILD_DRAW_FOUR)) {
            if (cardToPlayType.equals(UnoCard.UnoCardType.SKIP)) {
                // since the card is a skip we increment the turns twice
                skipCard(cardToPlay);
                // the card is a wild draw 4
            } else if (cardToPlay.equals(UnoCard.UNO_WILD_DRAW_FOUR)) {
                wildDrawFourCard(cardToPlay, lastPlayedCard);
                // the card is a reverse
            } else if (cardToPlayType.equals(UnoCard.UnoCardType.REVERSE)) {
                reverseCard(cardToPlay);
                // the card is a wild
            } else if (cardToPlayType.equals(UnoCard.UnoCardType.WILD) && color != null) {
                wildCard(cardToPlay, color);
            }
        }
        checkCardsLeft(user);
    }

    private void checkCardsLeft(LobbyEntry user) {
        if (this.playerData.get(user).size() == 1) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setDescription(String.format("%s has one card left.", user.getMention()));
            embed.setColor(Color.BLUE);
            this.channel.sendMessageEmbeds(embed.build()).queueAfter(1, TimeUnit.SECONDS);
            this.players.forEach(player -> {
                if (!player.isBot()) {
                    player.getUser().openPrivateChannel().queue(privateChannel -> {
                        privateChannel.sendMessageEmbeds(embed.build()).queueAfter(1, TimeUnit.SECONDS);
                    });
                }
            });
        }
    }

    private void skipCard(@NotNull UnoCard cardToPlay) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = cardToPlay.getColor();
        this.nextRound();
        this.nextRound();
        this.playedCards.add(cardToPlay);
    }

    private void drawTwoCard(@NotNull UnoCard cardToPlay) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = cardToPlay.getColor();
        this.amountToGrab += 2;
        this.nextRound();
        this.playedCards.add(cardToPlay);
    }

    private void wildDrawFourCard(@NotNull UnoCard cardToPlay, @NotNull UnoCard lastPlayedCard) {
        this.currentColour = lastPlayedCard.getColor();
        this.lastPlayedCard = cardToPlay;
        this.amountToGrab += 4;
        this.nextRound();
        this.playedCards.add(cardToPlay);
    }

    private void reverseCard(@NotNull UnoCard cardToPlay) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = cardToPlay.getColor();
        this.isReversed = !this.isReversed;
        this.nextRound();
        this.playedCards.add(cardToPlay);
    }

    private void wildCard(@NotNull UnoCard cardToPlay, @NotNull Color color) {
        this.lastPlayedCard = cardToPlay;
        this.currentColour = color;
        this.nextRound();
        this.playedCards.add(cardToPlay);
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            new ArrayList<>(unoGames).forEach(game -> game.onMessage(event));
        }
    }

    private class UnoPlayerData {

        private final User user;
        private int totalCardsPlayed;
        private int totalCardsDrawn;

        public UnoPlayerData(User user) {
            this.user = user;
            this.totalCardsPlayed = 0;
            this.totalCardsDrawn = 0;
        }

        public User getUser() {
            return user;
        }

        public int getTotalCardsPlayed() {
            return totalCardsPlayed;
        }

        public int getTotalCardsDrawn() {
            return totalCardsDrawn;
        }

        public void incrementTotalCardsPlayed() {
            this.totalCardsPlayed++;
        }

        public void incrementTotalCardsDrawn() {
            this.totalCardsDrawn++;
        }
    }

}
