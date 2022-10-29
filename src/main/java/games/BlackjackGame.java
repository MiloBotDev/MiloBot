package games;

import commands.games.blackjack.BlackjackPlayCmd;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import models.cards.CardDeck;
import models.cards.PlayingCard;
import database.dao.BlackjackDao;
import database.dao.UserDao;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;

public class BlackjackGame {

    private static final Map<Long, BlackjackGame> blackjackGames = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService idleInstanceCleanupExecutorService =
            Executors.newScheduledThreadPool(1);
    private volatile ScheduledFuture<?> idleInstanceCleanupFuture;
    private static final Logger logger = LoggerFactory.getLogger(BlackjackGame.class);
    private final List<PlayingCard> playerHand;
    private final List<PlayingCard> dealerHand;
    private final CardDeck<PlayingCard> deck;
    private final int playerBet;
    private final long userDiscordId;
    private static final UserDao userDao = UserDao.getInstance();
    private static final BlackjackDao blackjackDao = BlackjackDao.getInstance();
    private boolean playerStand;
    private boolean dealerStand;
    private boolean finished;
    private int winnings;
    private volatile Message message;

    private BlackjackGame(long userDiscordId, int bet) {
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.deck = new CardDeck<>(List.of(PlayingCard.values()));
        this.userDiscordId = userDiscordId;
        this.playerBet = bet;
        this.winnings = 0;
    }

    public static void newGame(MessageReceivedEvent event, List<String> args) {
        long authorIdLong = event.getAuthor().getIdLong();
        String authorId = event.getAuthor().getId();

        int bet = 0;
        if (args.size() > 0) {
            try {
                int playerBet = Integer.parseInt(args.get(0));
                if (playerBet < 0) {
                    event.getChannel().sendMessage("You can't bet a negative amount of Morbcoins.").queue();
                    return;
                } else if (playerBet == 0) {
                    event.getChannel().sendMessage("You can't bet `0` Morbcoins.").queue();
                    return;
                } else if (playerBet > 10000) {
                    event.getChannel().sendMessage("You can't bet more than `10000` Morbcoins.").queue();
                    return;
                } else if (blackjackGames.containsKey(authorIdLong)) {
                    event.getChannel().sendMessage("You are already in a game of blackjack.").queue();
                    return;
                } else {
                    try (Connection con = DatabaseConnection.getConnection()) {
                        con.setAutoCommit(false);
                        database.model.User user = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE);
                        int playerWallet = Objects.requireNonNull(user).getCurrency();
                        int newWallet = playerWallet - playerBet;
                        if (newWallet < 0) {
                            event.getChannel().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", playerBet, playerWallet)).queue();
                            con.commit();
                            return;
                        }
                        user.setCurrency(newWallet);
                        userDao.update(con, user);
                        if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                            blackjackDao.add(con, new database.model.Blackjack(Objects.requireNonNull(
                                    userDao.getUserByDiscordId(con, authorIdLong, RowLockType.NONE)).getId()));
                        }
                        con.commit();
                    } catch (SQLException e) {
                        logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                        return;
                    }
                    bet = playerBet;
                }
            } catch (NumberFormatException e) {
                event.getChannel().sendMessage("Invalid bet amount.").queue();
                return;
            }
        }

        BlackjackGame blackJack = new BlackjackGame(authorIdLong, bet);
        blackJack.initializeGame();


        BlackjackGame.BlackjackStates blackjackStates = blackJack.checkWin(false);
        EmbedBuilder embed;
        if (blackjackStates.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
            blackJack.dealerHit();
            blackJack.setDealerStand(true);
            blackjackStates = blackJack.checkWin(true);
            embed = blackJack.generateBlackjackEmbed(event.getAuthor(), blackjackStates);
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = blackJack.generateBlackjackEmbed(event.getAuthor(), null);
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue(msg -> {
                blackJack.message = msg;
                blackJack.setIdleInstanceCleanup();
                blackjackGames.put(authorIdLong, blackJack);
            });
        }
    }

    public static void newGame(SlashCommandEvent event) {
        long authorIdLong = event.getUser().getIdLong();
        String authorId = event.getUser().getId();

        if (blackjackGames.containsKey(authorIdLong)) {
            event.getHook().sendMessage("You are already in a game of blackjack.").queue();
            return;
        }

        int bet;
        if (event.getOption("bet") == null) {
            bet = 0;
            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                    blackjackDao.add(con, new database.model.Blackjack(Objects.requireNonNull(
                            userDao.getUserByDiscordId(con, authorIdLong, RowLockType.NONE)).getId()));
                }
                con.commit();
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                return;
            }
        } else {
            bet = Math.toIntExact(Objects.requireNonNull(event.getOption("bet")).getAsLong());
            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                database.model.User user = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
                int playerWallet = Objects.requireNonNull(user).getCurrency();
                int newWallet = playerWallet - bet;
                if (newWallet < 0) {
                    event.getHook().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
                    con.commit();
                    return;
                }
                user.setCurrency(newWallet);
                userDao.update(con, user);
                if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                    blackjackDao.add(con, new database.model.Blackjack(Objects.requireNonNull(
                            userDao.getUserByDiscordId(con, authorIdLong, RowLockType.NONE)).getId()));
                }
                con.commit();
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                return;
            }
        }

        BlackjackGame blackJack = new BlackjackGame(authorIdLong, bet);
        blackJack.initializeGame();

        BlackjackGame.BlackjackStates blackjackStates = blackJack.checkWin(false);
        EmbedBuilder embed;
        if (blackjackStates.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
            blackJack.dealerHit();
            blackJack.setDealerStand(true);
            blackjackStates = blackJack.checkWin(true);
            embed = blackJack.generateBlackjackEmbed(event.getUser(), blackjackStates);
            event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = blackJack.generateBlackjackEmbed(event.getUser(), null);
            event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue(msg -> {
                blackJack.message = msg;
                blackJack.setIdleInstanceCleanup();
                blackjackGames.put(authorIdLong, blackJack);
            });
        }
    }

    public static void replayBlackjack(ButtonClickEvent event) {
        String authorId = event.getUser().getId();
        if (blackjackGames.containsKey(event.getUser().getIdLong())) {
            return;
        }
        String description = event.getMessage().getEmbeds().get(0).getDescription();
        BlackjackGame value;
        if (description == null) {
            value = new BlackjackGame(event.getUser().getIdLong(), 0);
        } else {
            String s = description.replaceAll("[^0-9]", "");
            int bet = Integer.parseInt(s);
            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                database.model.User user2 = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
                int playerWallet = Objects.requireNonNull(user2).getCurrency();
                int newWallet = playerWallet - bet;
                if (newWallet < 0) {
                    event.reply(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
                    con.commit();
                    return;
                }
                user2.setCurrency(newWallet);
                userDao.update(con, user2);
                con.commit();
                value = new BlackjackGame(event.getUser().getIdLong(), bet);
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to replay blackjack.", e);
                return;
            }
        }

        value.initializeGame();
        BlackjackGame.BlackjackStates state = value.checkWin(false);
        EmbedBuilder embed;
        if (state.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
            value.dealerHit();
            value.setDealerStand(true);
            BlackjackStates blackjackStates = value.checkWin(true);
            embed = value.generateBlackjackEmbed(event.getUser(), blackjackStates);
            event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = value.generateBlackjackEmbed(event.getUser(), null);
            event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue();
            value.message = event.getMessage();
            value.setIdleInstanceCleanup();
            blackjackGames.put(event.getUser().getIdLong(), value);
        }
    }

    private @NotNull EmbedBuilder generateBlackjackEmbed(@NotNull User user, BlackjackGame.BlackjackStates state) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle("Blackjack");

        if (getPlayerBet() > 0) {
            embed.setDescription("You have bet `" + getPlayerBet() + "` Morbcoins.");
        }

        embed.addField("------------", "**Dealer Hand**", false);
        List<PlayingCard> dealerHand = getDealerHand();
        for (int i = 0; i < dealerHand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), dealerHand.get(i).getLabel(), true);
        }
        embed.addField("Total", String.format("%d", calculateHandValue(dealerHand)), false);

        embed.addField("------------", "**Player Hand**", false);
        List<PlayingCard> playerHand = getPlayerHand();
        for (int i = 0; i < playerHand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), playerHand.get(i).getLabel(), true);
        }
        embed.addField("Total", String.format("%d", calculateHandValue(playerHand)), false);

        if (state != null) {
            if (!isDealerStand()) {
                if (state.equals(BlackjackGame.BlackjackStates.DEALER_WIN)) {
                    String value = "**Dealer Wins!**\n";
                    if (getPlayerBet() > 0) {
                        value += String.format("You lose `%d` Morbcoins!\n", getWinnings());
                    }
                    embed.addField("------------", value, false);
                    setFinished(true);
                }
            } else {
                if (state.equals(BlackjackGame.BlackjackStates.PLAYER_WIN)) {
                    String format = String.format("**%s** wins!\n", user.getName());
                    if (getPlayerBet() > 0) {
                        format += String.format("You win `%d` Morbcoins!", getWinnings());
                    }
                    embed.addField("------------", format, false);
                    setFinished(true);
                } else if (state.equals(BlackjackGame.BlackjackStates.DRAW)) {
                    String value = "Its a draw!\n";
                    if (getPlayerBet() > 0) {
                        value += "You lose nothing.";
                    }
                    embed.addField("------------", value, false);
                    setFinished(true);
                } else if (state.equals(BlackjackGame.BlackjackStates.DEALER_WIN)) {
                    String format = "Dealer wins!\n";
                    if (getPlayerBet() > 0) {
                        format += String.format("You lose `%d` Morbcoins!", getWinnings());
                    }
                    embed.addField("------------", format, false);
                    setFinished(true);
                } else if (state.equals(BlackjackGame.BlackjackStates.DEALER_BLACKJACK)) {
                    String format = "Dealer wins with blackjack!\n";
                    if (getPlayerBet() > 0) {
                        format += String.format("You lose `%d` Morbcoins!", getWinnings());
                    }
                    embed.addField("------------", format, false);
                    setFinished(true);
                } else if (state.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
                    String format = String.format("**%s** wins with blackjack!\n", user.getName());
                    if (getPlayerBet() > 0) {
                        format += String.format("You win `%d` Morbcoins!", getWinnings());
                    }
                    embed.addField("------------", format, false);
                    setFinished(true);
                }
            }

        }
        return embed;
    }

    private void initializeGame() {
        this.dealerHand.add(deck.drawCard());
        this.playerHand.add(deck.drawCard());
        this.playerHand.add(deck.drawCard());
        this.playerStand = false;
        this.dealerStand = false;
        this.finished = false;
    }

    private void updateWallet(Connection con, @Nullable BlackjackStates state) throws SQLException {
        database.model.User user;
        user = userDao.getUserByDiscordId(con, userDiscordId, RowLockType.FOR_UPDATE);
        int playerWallet = Objects.requireNonNull(user).getCurrency();
        int newWallet;
        if (state == null) {
            newWallet = playerWallet - playerBet;
        } else {
            if (state.equals(BlackjackStates.PLAYER_BLACKJACK)) {
                this.winnings = (int) Math.ceil(((double) playerBet) * 1.5d);
                int ceil = (int) Math.ceil(((double) playerBet) * 2.5d);
                newWallet = playerWallet + ceil;
            } else if (state.equals(BlackjackStates.PLAYER_WIN)) {
                this.winnings = playerBet;
                int i = winnings * 2;
                newWallet = playerWallet + i;
            } else if (state.equals(BlackjackStates.DRAW)) {
                this.winnings = playerBet;
                newWallet = playerWallet + playerBet;
            } else {
                this.winnings = playerBet;
                newWallet = playerWallet;
            }
        }
        user.setCurrency(newWallet);
        userDao.update(con, user);
    }

    private void playerHit() {
        this.playerHand.add(deck.drawCard());
    }

    private void dealerHit() {
        this.dealerHand.add(deck.drawCard());
    }

    private void dealerMoves() {
        while (calculateHandValue(dealerHand) < 17) {
            dealerHit();
        }
    }

    private int calculateHandValue(@NotNull List<PlayingCard> hand) {
        int total = 0;
        ArrayList<PlayingCard> aces = new ArrayList<>();
        for (PlayingCard card : hand) {
            total += getCardValue(card);
            if (card.equals(PlayingCard.ACE_OF_SPADES) || card.equals(PlayingCard.ACE_OF_HEARTS) ||
                    card.equals(PlayingCard.ACE_OF_DIAMONDS) || card.equals(PlayingCard.ACE_OF_CLUBS)) {
                aces.add(card);
            }
        }
        // TODO: replace with a simple int since were not accessing the list
        for (PlayingCard ace : aces) {
            if (total > 21) {
                total -= 10;
            }
        }
        return total;
    }

    private int getCardValue(PlayingCard card) {
        PlayingCard.Rank rank = card.getRank();
        if (rank.toInt() <= 10) {
            // card with number on its face
            return rank.toInt();
        } else if (rank == PlayingCard.Rank.ACE) {
            return 11;
        } else {
            // king, queen, or jack
            return 10;
        }
    }

    private BlackjackStates checkWin(boolean updateDb) {
        int playerValue = calculateHandValue(playerHand);
        int dealerValue = calculateHandValue(dealerHand);
        BlackjackStates state;
        if (playerValue == 21 && dealerValue == 21) {
            state = BlackjackStates.DRAW;
        } else {
            if (playerValue == 21 && playerHand.size() == 2) {
                state = BlackjackStates.PLAYER_BLACKJACK;
            } else if (dealerValue == 21 && dealerHand.size() == 2) {
                state = BlackjackStates.DEALER_BLACKJACK;
            } else {
                if (playerValue > 21) {
                    state = BlackjackStates.DEALER_WIN;
                } else if (dealerValue > 21) {
                    state = BlackjackStates.PLAYER_WIN;
                } else {
                    if (playerValue > dealerValue) {
                        state = BlackjackStates.PLAYER_WIN;
                    } else if (playerValue < dealerValue) {
                        state = BlackjackStates.DEALER_WIN;
                    } else {
                        state = BlackjackStates.DRAW;
                    }
                }
            }
        }
        if (updateDb) {
            updateBlackjackDatabase(state);
        }
        return state;
    }

    private void updateBlackjackDatabase(BlackjackStates state) {
        database.model.Blackjack blackjack;
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            updateWallet(con, state);
            blackjack = Objects.requireNonNull(blackjackDao.getByUserDiscordId(con, userDiscordId, RowLockType.FOR_UPDATE));
            if (state.equals(BlackjackStates.DRAW)) {
                blackjack.addGame(database.model.Blackjack.BlackjackResult.DRAW, 0);
            } else if (state.equals(BlackjackStates.PLAYER_BLACKJACK) || state.equals(BlackjackStates.PLAYER_WIN)) {
                blackjack.addGame(database.model.Blackjack.BlackjackResult.WIN, this.winnings);
            } else {
                // it's a loss;
                blackjack.addGame(database.model.Blackjack.BlackjackResult.LOSS, -this.winnings);
            }
            blackjackDao.update(con, blackjack);
            con.commit();
        } catch (SQLException e) {
            logger.error("Error updating user blackjack entry.", e);
        }
    }

    private boolean cancelIdleInstanceCleanup() {
        return idleInstanceCleanupFuture.cancel(false);
    }

    private void setIdleInstanceCleanup() {
        idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
            blackjackGames.remove(userDiscordId);
            message.delete().queue();
        }, 5, TimeUnit.SECONDS);
    }

    public static BlackjackGame getGameByAuthorId(long authorId) {
        return blackjackGames.get(authorId);
    }

    public void hit(ButtonClickEvent event) {
        if (!cancelIdleInstanceCleanup()) {
            return;
        }

        if (isFinished() || isPlayerStand()) {
            return;
        }
        playerHit();
        BlackjackGame.BlackjackStates blackjackStates = checkWin(false);
        EmbedBuilder newEmbed;
        if (blackjackStates.equals(BlackjackGame.BlackjackStates.DEALER_WIN)) {
            checkWin(true);
            newEmbed = generateBlackjackEmbed(event.getUser(), blackjackStates);
            event.editMessageEmbeds(newEmbed.build()).setActionRows(ActionRow.of(
                    Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                    Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
            blackjackGames.remove(event.getUser().getIdLong());
        } else {
            setIdleInstanceCleanup();
            newEmbed = generateBlackjackEmbed(event.getUser(), null);
            event.editMessageEmbeds(newEmbed.build()).queue();
        }
    }

    public void stand(ButtonClickEvent event) {
        if (!cancelIdleInstanceCleanup()) {
            return;
        }

        if (isFinished() || isPlayerStand()) {
            return;
        }
        setPlayerStand(true);
        dealerMoves();
        setDealerStand(true);
        BlackjackGame.BlackjackStates blackjackStates = checkWin(true);
        EmbedBuilder embedBuilder = generateBlackjackEmbed(event.getUser(), blackjackStates);
        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(
                Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
        blackjackGames.remove(event.getUser().getIdLong());
    }

    private List<PlayingCard> getPlayerHand() {
        return playerHand;
    }

    private List<PlayingCard> getDealerHand() {
        return dealerHand;
    }

    private boolean isPlayerStand() {
        return playerStand;
    }

    private void setPlayerStand(boolean playerStand) {
        this.playerStand = playerStand;
    }

    private boolean isDealerStand() {
        return dealerStand;
    }

    private void setDealerStand(boolean dealerStand) {
        this.dealerStand = dealerStand;
    }

    private boolean isFinished() {
        return finished;
    }

    private void setFinished(boolean finished) {
        this.finished = finished;
    }

    private int getPlayerBet() {
        return playerBet;
    }

    private int getWinnings() {
        return winnings;
    }

    public enum BlackjackStates {
        PLAYER_WIN,
        PLAYER_BLACKJACK,
        DEALER_WIN,
        DEALER_BLACKJACK,
        DRAW
    }
}
