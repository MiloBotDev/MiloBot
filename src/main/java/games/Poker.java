package games;

import models.cards.CardDeck;
import models.cards.PlayingCards;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.*;
import java.util.stream.Collectors;

public class Poker {
    private static final ArrayList<Poker> games = new ArrayList<>();
    private final List<User> players;
    private final Map<User, PlayerData> playerData = new HashMap<>();
    private final CardDeck mainDeck = new CardDeck();
    private PokerState state;
    private int nextPlayerIndex;
    private int playerRaise;
    private int requiredMoneyInPot;
    private boolean waitingForUserRaise;
    private boolean firstRound;
    private int lastRaisePlayerIndex;
    private int remainingPlayers;
    private final Random random = new Random();
    private final Set<PlayerAction> authorizedActions = new HashSet<>();

    public enum PokerState {
        WAITING_TO_START,
        BET_ROUND_1,
        REPLACING_CARDS,
        BET_ROUND_2,
        GAME_END
    }

    public enum PlayerAction {
        FOLD,
        CALL,
        RAISE,
        CHECK
    }

    private static class PlayerData {
        private volatile Message embed;
        private List<PlayingCards> hand;
        private int moneyInPot = 0;
        private boolean inGame = true;
        private boolean replacedCards = false;
    }

    public Poker(List<User> players) {
        games.add(this);
        this.players = new ArrayList<>(players);
        state = PokerState.WAITING_TO_START;
    }

    public void start() {
        if (players.size() < 2) {
            throw new IllegalStateException("Need at least 2 players to start a game");
        } else {
            players.forEach(player -> {
                playerData.put(player, new PlayerData());
                List<PlayingCards> hand = new ArrayList<>();
                for (int i = 0; i < 5; i++) {
                    hand.add(mainDeck.drawCard());
                }
                sortHand(hand);
                playerData.get(player).hand = hand;
            });
            next();
        }
    }

    private void next() {
        if (state == PokerState.WAITING_TO_START) {
            firstRound = true;
            waitingForUserRaise = false;
            requiredMoneyInPot = 0;
            lastRaisePlayerIndex = -1;
            nextPlayerIndex = -1;
            remainingPlayers = players.size();
            state = PokerState.BET_ROUND_1;
            next();
        } else if (state == PokerState.BET_ROUND_1) {
            removeUserActions();
            if (remainingPlayers == 1) { // everyone else folded
                endGame();
            } else if (((!firstRound && lastRaisePlayerIndex == nextPlayerIndex) ||
                    (nextPlayerIndex == players.size() - 1 && lastRaisePlayerIndex == -1)) && !waitingForUserRaise) {
                state = PokerState.REPLACING_CARDS;
                players.stream().filter(player -> playerData.get(player).inGame)
                        .forEach(player -> player.openPrivateChannel().queue(channel ->
                                channel.sendMessage("Which cards would you like to replace? Enter card numbers " +
                                        "separated by spaces, or 'none', if you don't want to replace any.").queue()));
            } else {
                if (waitingForUserRaise) {
                    processPlayerRaise();
                }
                nextPlayer();
                User user = players.get(nextPlayerIndex);
                EmbedBuilder eb = generatePlayerEmbed(user);
                ActionRow actionRow = getUserActions();
                if (firstRound && nextPlayerIndex == 0) {
                    players.stream().filter(player -> !player.equals(user)).forEach(player ->
                            player.openPrivateChannel()
                            .queue(channel ->
                                    channel.sendMessageEmbeds(generatePlayerEmbed(player).build())
                                            .queue(message -> playerData.get(player).embed = message)));
                    user.openPrivateChannel().queue(channel ->
                            channel.sendMessageEmbeds(eb.build()).setActionRows(actionRow)
                                    .queue(message -> playerData.get(user).embed = message));
                } else {
                    playerData.get(user).embed.editMessageEmbeds(eb.build()).setActionRows(actionRow).queue();
                }
            }
        } else if (state == PokerState.REPLACING_CARDS) {
            if (players.stream().filter(player -> playerData.get(player).inGame)
                    .allMatch(player -> playerData.get(player).replacedCards)) {
                firstRound = true;
                lastRaisePlayerIndex = -1;
                nextPlayerIndex = -1;
                state = PokerState.BET_ROUND_2;
                next();
            }
        } else if (state == PokerState.BET_ROUND_2) {
            removeUserActions();
            if (remainingPlayers == 1) { // everyone else folded
                endGame();
            } else if (((!firstRound && lastRaisePlayerIndex == nextPlayerIndex) ||
                    (nextPlayerIndex == players.size() - 1 && lastRaisePlayerIndex == -1)) && !waitingForUserRaise) {
                state = PokerState.GAME_END;
                int maxHandVal = players.stream().filter(player -> playerData.get(player).inGame)
                        .map(player -> playerData.get(player).hand).mapToInt(Hands::getHandValue)
                        .max().orElse(0);
                List<User> winners = players.stream().filter(player -> playerData.get(player).inGame)
                        .filter(player -> Hands.getHandValue(playerData.get(player).hand) == maxHandVal).toList();
                int moneyWon = requiredMoneyInPot / winners.size();
                // left over money if money can't be evenly divided between winning players
                int leftoverMoney = requiredMoneyInPot % winners.size();
                // lucky winner is winner who gets money if there is leftover money
                User luckyWinner = winners.get(random.nextInt(winners.size()));
                players.forEach(player -> {
                    EmbedBuilder eb = generatePlayerEmbed(player);
                    eb.addBlankField(false);
                    String winnerStr;
                    if (winners.size() > 1) {
                        winnerStr = "Winners";
                    } else {
                        winnerStr = "Winner";
                    }
                    eb.addField(winnerStr, winners.stream().map(User::getAsMention).collect(Collectors.joining(", ")),
                            true);
                    if (leftoverMoney == 0) {
                        eb.addField("Money won", moneyWon + " Morbcoins", true);
                    } else {
                        eb.addField("Money won", moneyWon + " Morbcoins (" + luckyWinner.getAsMention() +
                                " won " + (moneyWon + leftoverMoney) + ")", true);
                    }
                    playerData.get(player).embed.editMessageEmbeds(eb.build()).queue();
                });
                games.remove(this);
            } else {
                if (waitingForUserRaise) {
                    processPlayerRaise();
                }
                nextPlayer();
                User user = players.get(nextPlayerIndex);
                ActionRow actionRow = getUserActions();
                EmbedBuilder eb = generatePlayerEmbed(user);
                playerData.get(user).embed.editMessageEmbeds(eb.build()).setActionRows(actionRow).queue();
            }
        }
    }

    private void removeUserActions() {
        if (nextPlayerIndex >= 0) {
            User user = players.get(nextPlayerIndex);
            EmbedBuilder eb = generatePlayerEmbed(user);
            playerData.get(user).embed.editMessageEmbeds(eb.build()).setActionRows().queue();
        }
    }

    private void endGame() {
        state = PokerState.GAME_END;
        User winningPlayer = Objects.requireNonNull(
                players.stream().filter(player -> playerData.get(player).inGame).findFirst().orElse(null));
        players.forEach(player -> {
            EmbedBuilder eb = generatePlayerEmbed(player);
            eb.addBlankField(false);
            eb.addField("Winner", winningPlayer.getAsMention(), true);
            eb.addField("Money won", requiredMoneyInPot + " Morbcoins", true);
            playerData.get(player).embed.editMessageEmbeds(eb.build()).queue();
        });
        state = PokerState.GAME_END;
        games.remove(this);
    }

    private void processPlayerRaise() {
        int raise = playerRaise;
        requiredMoneyInPot += raise;
        playerData.get(players.get(nextPlayerIndex)).moneyInPot = requiredMoneyInPot;
        lastRaisePlayerIndex = nextPlayerIndex;
        waitingForUserRaise = false;
        User user = players.get(nextPlayerIndex);
        EmbedBuilder eb = generatePlayerEmbed(user);
        playerData.get(user).embed.editMessageEmbeds(eb.build()).queue();
    }

    private void nextPlayer() {
        do {
            if (nextPlayerIndex == players.size() - 1) {
                nextPlayerIndex = 0;
                firstRound = false;
            } else {
                nextPlayerIndex++;
            }
        } while (!playerData.get(players.get(nextPlayerIndex)).inGame);
    }

    private ActionRow getUserActions() {
        User user = players.get(nextPlayerIndex);
        String authorId = user.getId();
        Button check = Button.primary(authorId + ":poker_check", "Check");
        Button fold = Button.primary(authorId + ":poker_fold", "Fold");
        Button call = Button.primary(authorId + ":poker_call", "Call");
        Button raise = Button.primary(authorId + ":poker_raise", "Raise");
        int moneyInPot = playerData.get(user).moneyInPot;
        ActionRow actionRow;
        if (moneyInPot < requiredMoneyInPot) {
            actionRow = ActionRow.of(fold, call, raise);
            authorizedActions.addAll(List.of(PlayerAction.FOLD, PlayerAction.CALL, PlayerAction.RAISE));
        } else {
            actionRow = ActionRow.of(check, raise);
            authorizedActions.addAll(List.of(PlayerAction.CHECK, PlayerAction.RAISE));
        }
        return actionRow;
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot() && event.getChannelType() == ChannelType.PRIVATE) {
            new ArrayList<>(games).forEach(game -> game.onMessage(event));
        }
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        if ((state == PokerState.BET_ROUND_1 || state == PokerState.BET_ROUND_2) &&
                event.getAuthor().equals(players.get(nextPlayerIndex)) && waitingForUserRaise) {
            try {
                playerRaise = Integer.parseInt(event.getMessage().getContentRaw());
                if (playerRaise >= 1) {
                    next();
                } else {
                    event.getChannel().sendMessage("Raise must be at least 1.").queue();
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Please enter a number.").queue();
            }
        } else if (state == PokerState.REPLACING_CARDS) {
            if (playerData.get(event.getAuthor()).inGame && !playerData.get(event.getAuthor()).replacedCards) {
                String msg = event.getMessage().getContentRaw().strip();
                if (msg.equals("none")) {
                    event.getChannel().sendMessage("OK, no cards have been replaced.").queue();
                } else {
                    String[] cards = msg.split(" ");
                    Set<Integer> cardsToReplace = new HashSet<>();
                    for (String card : cards) {
                        try {
                            int cardNumber = Integer.parseInt(card);
                            if (cardNumber >= 1 && cardNumber <= 5) {
                                if (!cardsToReplace.add(cardNumber)) {
                                    event.getChannel().sendMessage("No duplicate cards allowed.").queue();
                                    return;
                                }
                            } else {
                                event.getChannel().sendMessage("Card numbers must be between 1 and 5.").queue();
                                return;
                            }
                        } catch (NumberFormatException e) {
                            event.getChannel().sendMessage("Please enter valid card numbers separated by spaces, or " +
                                            "'none', if you wouldn't like to replace any.")
                                    .queue();
                            return;
                        }
                    }
                    if (cardsToReplace.size() < 1 || cardsToReplace.size() > 3) {
                        event.getChannel().sendMessage("Please enter 1-3 card numbers.").queue();
                        return;
                    }
                    List<PlayingCards> hand = playerData.get(event.getAuthor()).hand;
                    for (Integer cardNumber : cardsToReplace) {
                        hand.set(cardNumber - 1, mainDeck.drawCard());
                    }
                    sortHand(hand);
                    playerData.get(event.getAuthor()).embed.editMessageEmbeds(
                            generatePlayerEmbed(event.getAuthor()).build()).queue();
                    event.getChannel().sendMessage("Your cards have been replaced.").queue();
                }
                playerData.get(event.getAuthor()).replacedCards = true;
                next();
            }
        }
    }

    private EmbedBuilder generatePlayerEmbed(User user) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle("Poker");
        embed.addField("------------", "**Your hand**", false);
        List<PlayingCards> hand = playerData.get(user).hand;
        for(int i = 0; i < hand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), hand.get(i).getLabel(), true);
        }
        embed.addBlankField(false);
        embed.addField("Your bet", String.valueOf(playerData.get(user).moneyInPot), true);
        embed.addField("Required money in pot", String.valueOf(requiredMoneyInPot), true);
        return embed;
    }

    public static Poker getUserGame(User user) {
        return games.stream().filter(game -> game.players.contains(user)).findAny().orElse(null);
    }

    public void setPlayerAction(PlayerAction action) {
        if (authorizedActions.contains(action)) {
            authorizedActions.clear();
            switch (action) {
                case CHECK -> next();
                case FOLD -> {
                    playerData.get(players.get(nextPlayerIndex)).inGame = false;
                    remainingPlayers--;
                    next();
                }
                case CALL -> {
                    playerData.get(players.get(nextPlayerIndex)).moneyInPot = requiredMoneyInPot;
                    next();
                }
                case RAISE -> players.get(nextPlayerIndex).openPrivateChannel().queue(channel ->
                        channel.sendMessage("How much would you like to raise by?")
                                .queue(msg -> waitingForUserRaise = true));
            }
        }
    }

    private void sortHand(List<PlayingCards> hand) {
        Comparator<PlayingCards> c1 = Comparator.comparingInt(o -> o.getRank().toInt());
        Comparator<PlayingCards> c2 = c1.thenComparing(PlayingCards::getSuit);
        hand.sort(c2);
    }

    public static class Hands {
        public static int getHandValue(List<PlayingCards> hand) {
            int[] vals = {isRoyalFlush(hand), isStraightFlush(hand), isFourOfAKind(hand), isFullHouse(hand),
                    isFlush(hand), isStraight(hand), isThreeOfAKind(hand), isTwoPair(hand), isPair(hand)};
            return Arrays.stream(vals).filter(val -> val != 0).findFirst().orElse(
                    hand.stream().mapToInt(c -> c.getRank().toInt()).max().orElse(0));
        }

        private static int isRoyalFlush(List<PlayingCards> hand) {
            int straightFlush = isStraightFlush(hand);
            if (straightFlush != 0 && hand.get(0).getRank() == PlayingCards.Rank.TEN) {
                return 0x8000;
            }
            return 0;
        }

        private static int isStraightFlush(List<PlayingCards> hand) {
            int flush = isFlush(hand);
            int straight = isStraight(hand);
            if (flush != 0 && straight != 0) {
                return flush & 0x0F | 0x4000;
            }
            return 0;
        }

        private static int isFourOfAKind(List<PlayingCards> hand) {
            for (int i = 0; i < hand.size() - 3; i++) {
                if (hand.get(i).getRank() == hand.get(i + 1).getRank() &&
                        hand.get(i).getRank() == hand.get(i + 2).getRank() &&
                        hand.get(i).getRank() == hand.get(i + 3).getRank()) {
                    return hand.get(i).getRank().toInt() | 0x2000;
                }
            }
            return 0;
        }

        private static int isFullHouse(List<PlayingCards> hand) {
            int threeOfAKind = isThreeOfAKind(hand);
            int pair = isPair(hand);
            if (threeOfAKind != 0 && pair != 0) {
                return threeOfAKind & 0x0F | 0x1000;
            } else {
                return 0;
            }
        }

        private static int isFlush(List<PlayingCards> hand) {
            PlayingCards.Suit nextSuitReq = hand.get(0).getSuit();
            for (int i = 1; i < hand.size(); i++) {
                if (hand.get(i).getSuit() != nextSuitReq) {
                    return 0;
                }
            }
            return hand.get(hand.size() - 1).getRank().toInt() | 0x800;
        }

        private static int isStraight(List<PlayingCards> hand) {
            int nextRankReq = hand.get(0).getRank().toInt();
            for (int i = 1; i < hand.size(); i++) {
                nextRankReq++;
                if (hand.get(i).getRank().toInt() != nextRankReq) {
                    return 0;
                }
            }
            return nextRankReq | 0x400;
        }

        private static int isThreeOfAKind(List<PlayingCards> hand) {
            for (int i = 0; i < hand.size() - 2; i++) {
                if (hand.get(i).getRank() == hand.get(i + 1).getRank() &&
                        hand.get(i).getRank() == hand.get(i + 2).getRank()) {
                    return hand.get(i).getRank().toInt() | 0x200;
                }
            }
            return 0;
        }

        private static int isTwoPair(List<PlayingCards> hand) {
            int matches = 0;
            List<PlayingCards.Rank> alreadyMatched = new ArrayList<>();
            for (int i = 0; i < hand.size() - 1; i++) {
                if (hand.get(i).getRank() == hand.get(i + 1).getRank() &&
                        !alreadyMatched.contains(hand.get(i).getRank())) {
                    alreadyMatched.add(hand.get(i).getRank());
                    matches++;
                }
            }
            if (matches == 2) {
                int maxVal = 0;
                for (PlayingCards.Rank rank : alreadyMatched) {
                    if (rank.toInt() > maxVal) {
                        maxVal = rank.toInt();
                    }
                }
                return maxVal | 0x100;
            } else {
                return 0;
            }
        }

        private static int isPair(List<PlayingCards> hand) {
            for (int i = 0; i < hand.size() - 1; i++) {
                if (hand.get(i).getRank() == hand.get(i + 1).getRank() && (i == 3 ||
                        hand.get(i).getRank() != hand.get(i + 2).getRank()) && (i == 0 ||
                        hand.get(i).getRank() != hand.get(i - 1).getRank())) {
                    return hand.get(i).getRank().toInt() | 0x80;
                }
            }
            return 0;
        }
    }
}
