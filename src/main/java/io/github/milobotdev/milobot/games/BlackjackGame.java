package io.github.milobotdev.milobot.games;

import io.github.milobotdev.milobot.commands.games.blackjack.BlackjackPlayCmd;
import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.database.dao.BlackjackDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.Blackjack;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.models.cards.CardDeck;
import io.github.milobotdev.milobot.models.cards.PlayingCard;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;

public class BlackjackGame {

    public static final Map<Long, BlackjackGame> blackjackGames = new ConcurrentHashMap<>();
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
    private Message message;
    private static int duration;

    private BlackjackGame(long userDiscordId, int bet, int instanceDuration) {
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.deck = new CardDeck<>(List.of(PlayingCard.values()));
        this.userDiscordId = userDiscordId;
        this.playerBet = bet;
        this.winnings = 0;
        duration = instanceDuration;
    }

    public static void newGame(@NotNull MessageReceivedEvent event, @NotNull List<String> args, int instanceDuration) {
        duration = instanceDuration;
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
                        io.github.milobotdev.milobot.database.model.User user = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE);
                        int playerWallet = Objects.requireNonNull(user).getCurrency();
                        int newWallet = playerWallet - playerBet;
                        if (newWallet < 0) {
                            event.getChannel().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", playerBet, playerWallet)).queue();
                            con.commit();
                            return;
                        }
                        user.setCurrency(newWallet);
                        userDao.update(con, user);
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

        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                blackjackDao.add(con, new Blackjack(Objects.requireNonNull(
                        userDao.getUserByDiscordId(con, authorIdLong, RowLockType.NONE)).getId()));
            }
            con.commit();
        } catch (SQLException e) {
            logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
            return;
        }

        BlackjackGame blackjack = new BlackjackGame(authorIdLong, bet, duration);
        blackjack.initializeGame();


        BlackjackGame.BlackjackStates blackjackStates = blackjack.checkWin(false);
        EmbedBuilder embed;
        if (blackjackStates.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
            blackjack.dealerHit();
            blackjack.dealerStand = true;
            blackjackStates = blackjack.checkWin(true);
            embed = blackjack.generateBlackjackEmbed(event.getAuthor(), blackjackStates);
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = blackjack.generateBlackjackEmbed(event.getAuthor(), null);
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue(msg -> {
                blackjack.message = msg;
                blackjack.setIdleInstanceCleanup();
                blackjackGames.put(authorIdLong, blackjack);
            });
        }
    }

    public static void newGame(@NotNull SlashCommandInteractionEvent event, int instanceDuration) {
        duration = instanceDuration;
        long authorIdLong = event.getUser().getIdLong();
        String authorId = event.getUser().getId();

        int bet;
        if (event.getOption("bet") == null) {
            bet = 0;
            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                if (blackjackDao.getByUserDiscordId(con, authorIdLong, RowLockType.FOR_UPDATE) == null) {
                    blackjackDao.add(con, new Blackjack(Objects.requireNonNull(
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
                io.github.milobotdev.milobot.database.model.User user = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
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
                    blackjackDao.add(con, new Blackjack(Objects.requireNonNull(
                            userDao.getUserByDiscordId(con, authorIdLong, RowLockType.NONE)).getId()));
                }
                con.commit();
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to play blackjack.", e);
                return;
            }
        }

        BlackjackGame blackjack = new BlackjackGame(authorIdLong, bet, instanceDuration);
        blackjack.initializeGame();

        BlackjackGame.BlackjackStates blackjackStates = blackjack.checkWin(false);
        EmbedBuilder embed;
        if (blackjackStates.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
            blackjack.dealerHit();
            blackjack.dealerStand = true;
            blackjackStates = blackjack.checkWin(true);
            embed = blackjack.generateBlackjackEmbed(event.getUser(), blackjackStates);
            event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = blackjack.generateBlackjackEmbed(event.getUser(), null);
            event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue(msg -> {
                blackjack.message = msg;
                blackjack.setIdleInstanceCleanup();
                blackjackGames.put(authorIdLong, blackjack);
            });
        }
    }

    public static void replayBlackjack(@NotNull ButtonClickEvent event) {
        String authorId = event.getUser().getId();
        if (blackjackGames.containsKey(event.getUser().getIdLong())) {
            return;
        }
        String description = event.getMessage().getEmbeds().get(0).getDescription();
        BlackjackGame blackjack;
        if (description == null) {
            blackjack = new BlackjackGame(event.getUser().getIdLong(), 0, duration);
        } else {
            String s = description.replaceAll("[^0-9]", "");
            int bet = Integer.parseInt(s);
            try (Connection con = DatabaseConnection.getConnection()) {
                con.setAutoCommit(false);
                io.github.milobotdev.milobot.database.model.User user = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
                int playerWallet = Objects.requireNonNull(user).getCurrency();
                int newWallet = playerWallet - bet;
                if (newWallet < 0) {
                    event.reply(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
                    con.commit();
                    return;
                }
                user.setCurrency(newWallet);
                userDao.update(con, user);
                con.commit();
                blackjack = new BlackjackGame(event.getUser().getIdLong(), bet, duration);
            } catch (SQLException e) {
                logger.error("Error updating blackjack data when user wanted to replay blackjack.", e);
                return;
            }
            GameInstanceManager.getInstance().addUser(event.getUser().getIdLong(), BlackjackPlayCmd.instanceData.gameType(), duration);
        }

        blackjack.initializeGame();
        BlackjackGame.BlackjackStates state = blackjack.checkWin(false);
        EmbedBuilder embed;
        if (state.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
            blackjack.dealerHit();
            blackjack.dealerStand = true;
            BlackjackStates blackjackStates = blackjack.checkWin(true);
            embed = blackjack.generateBlackjackEmbed(event.getUser(), blackjackStates);
            event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":replayBlackjack", "Replay"),
                    Button.secondary(authorId + ":delete", "Delete")
            )).queue();
        } else {
            embed = blackjack.generateBlackjackEmbed(event.getUser(), null);
            event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                    Button.primary(authorId + ":stand", "Stand"),
                    Button.primary(authorId + ":hit", "Hit")
            )).queue();
            blackjack.message = event.getMessage();
            blackjack.setIdleInstanceCleanup();
            blackjackGames.put(event.getUser().getIdLong(), blackjack);
        }
    }

    private @NotNull EmbedBuilder generateBlackjackEmbed(@NotNull User user, BlackjackGame.BlackjackStates state) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle("Blackjack");

        if (playerBet > 0) {
            embed.setDescription("You have bet `" + playerBet + "` Morbcoins.");
        }

        embed.addField("------------", "**Dealer Hand**", false);
        for (int i = 0; i < dealerHand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), dealerHand.get(i).getLabel(), true);
        }
        embed.addField("Total", String.format("%d", calculateHandValue(dealerHand)), false);

        embed.addField("------------", "**Player Hand**", false);
        for (int i = 0; i < playerHand.size(); i++) {
            embed.addField(String.format("Card %d", i + 1), playerHand.get(i).getLabel(), true);
        }
        embed.addField("Total", String.format("%d", calculateHandValue(playerHand)), false);

        if (state != null) {
            if (!dealerStand) {
                if (state.equals(BlackjackGame.BlackjackStates.DEALER_WIN)) {
                    String value = "**Dealer Wins!**\n";
                    if (playerBet > 0) {
                        value += String.format("You lose `%d` Morbcoins!\n", winnings);
                    }
                    embed.addField("------------", value, false);
                    finished = true;
                }
            } else {
                if (state.equals(BlackjackGame.BlackjackStates.PLAYER_WIN)) {
                    String format = String.format("**%s** wins!\n", user.getName());
                    if (playerBet > 0) {
                        format += String.format("You win `%d` Morbcoins!", winnings);
                    }
                    embed.addField("------------", format, false);
                    finished = true;
                } else if (state.equals(BlackjackGame.BlackjackStates.DRAW)) {
                    String value = "Its a draw!\n";
                    if (playerBet > 0) {
                        value += "You lose nothing.";
                    }
                    embed.addField("------------", value, false);
                    finished = true;
                } else if (state.equals(BlackjackGame.BlackjackStates.DEALER_WIN)) {
                    String format = "Dealer wins!\n";
                    if (playerBet > 0) {
                        format += String.format("You lose `%d` Morbcoins!", winnings);
                    }
                    embed.addField("------------", format, false);
                    finished = true;
                } else if (state.equals(BlackjackGame.BlackjackStates.DEALER_BLACKJACK)) {
                    String format = "Dealer wins with blackjack!\n";
                    if (playerBet > 0) {
                        format += String.format("You lose `%d` Morbcoins!", winnings);
                    }
                    embed.addField("------------", format, false);
                    finished = true;
                } else if (state.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
                    String format = String.format("**%s** wins with blackjack!\n", user.getName());
                    if (playerBet > 0) {
                        format += String.format("You win `%d` Morbcoins!", winnings);
                    }
                    embed.addField("------------", format, false);
                    finished = true;
                }
            }

        }
        if(finished) {
            GameInstanceManager.getInstance().removeUserGame(user.getIdLong());
        }
        return embed;
    }

    private void initializeGame() {
        this.dealerHand.add(deck.drawCard().get());
        this.playerHand.add(deck.drawCard().get());
        this.playerHand.add(deck.drawCard().get());
        this.playerStand = false;
        this.dealerStand = false;
        this.finished = false;
    }

    private void updateWallet(Connection con, @Nullable BlackjackStates state) throws SQLException {
        io.github.milobotdev.milobot.database.model.User user;
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
        this.playerHand.add(deck.drawCard().get());
    }

    private void dealerHit() {
        this.dealerHand.add(deck.drawCard().get());
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

    private int getCardValue(@NotNull PlayingCard card) {
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
        Blackjack blackjack;
        try (Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            updateWallet(con, state);
            blackjack = Objects.requireNonNull(blackjackDao.getByUserDiscordId(con, userDiscordId, RowLockType.FOR_UPDATE));
            if (state.equals(BlackjackStates.DRAW)) {
                blackjack.addGame(Blackjack.BlackjackResult.DRAW, 0);
            } else if (state.equals(BlackjackStates.PLAYER_BLACKJACK) || state.equals(BlackjackStates.PLAYER_WIN)) {
                blackjack.addGame(Blackjack.BlackjackResult.WIN, this.winnings);
            } else {
                // it's a loss;
                blackjack.addGame(Blackjack.BlackjackResult.LOSS, -this.winnings);
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
        GameInstanceManager gameInstanceManager = GameInstanceManager.getInstance();
        idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
            blackjackGames.remove(userDiscordId);
            if(gameInstanceManager.containsUser(userDiscordId)) {
                gameInstanceManager.removeUserGame(userDiscordId);
            }
            message.delete().queue();
        }, 15, TimeUnit.MINUTES);
    }

    public static BlackjackGame getGameByAuthorId(long authorId) {
        return blackjackGames.get(authorId);
    }

    public void hit(ButtonClickEvent event) {
        if (!cancelIdleInstanceCleanup()) {
            return;
        }

        if (finished || playerStand) {
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

        if (finished || playerStand) {
            return;
        }
        playerStand = true;
        dealerMoves();
        dealerStand = true;
        BlackjackGame.BlackjackStates blackjackStates = checkWin(true);
        EmbedBuilder embedBuilder = generateBlackjackEmbed(event.getUser(), blackjackStates);
        event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(
                Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
        blackjackGames.remove(event.getUser().getIdLong());
    }

    public Message getMessage() {
        return message;
    }

    public enum BlackjackStates {
        PLAYER_WIN,
        PLAYER_BLACKJACK,
        DEALER_WIN,
        DEALER_BLACKJACK,
        DRAW
    }
}
