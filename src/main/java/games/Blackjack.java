package games;

import models.cards.CardDeck;
import models.cards.PlayingCard;
import database.dao.BlackjackDao;
import database.dao.UserDao;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Blackjack {

    private static final Logger logger = LoggerFactory.getLogger(Blackjack.class);
    private final List<PlayingCard> playerHand;
    private final List<PlayingCard> dealerHand;
    private final CardDeck<PlayingCard> deck;
    private final int playerBet;
    private final long userDiscordId;
    private final long startTime;
    private final UserDao userDao = UserDao.getInstance();
    private final BlackjackDao blackjackDao = BlackjackDao.getInstance();
    private boolean playerStand;
    private boolean dealerStand;
    private boolean finished;
    private int winnings;

    public Blackjack(long userDiscordId) {
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.deck = new CardDeck<>(List.of(PlayingCard.values()));
        this.userDiscordId = userDiscordId;
        this.playerBet = 0;
        this.winnings = 0;
        this.startTime = System.nanoTime();
    }

    public Blackjack(long userDiscordId, int bet) {
        this.playerHand = new ArrayList<>();
        this.dealerHand = new ArrayList<>();
        this.deck = new CardDeck<>(List.of(PlayingCard.values()));
        this.userDiscordId = userDiscordId;
        this.playerBet = bet;
        this.winnings = 0;
        updateWallet(null);
        this.startTime = System.nanoTime();
    }

    public void initializeGame() {
        this.dealerHand.add(deck.drawCard());
        this.playerHand.add(deck.drawCard());
        this.playerHand.add(deck.drawCard());
        this.playerStand = false;
        this.dealerStand = false;
        this.finished = false;
    }

    public void updateWallet(@Nullable BlackjackStates state) {
        database.model.User user;
        try {
            user = userDao.getUserByDiscordId(userDiscordId);
        } catch (SQLException e) {
            logger.error("Error getting user from database when user wanted to play blackjack.", e);
            return;
        }
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
        try {
            userDao.update(user);
        } catch (SQLException e) {
            logger.error("Blackjack error updating user in database when attempted to update wallet.", e);
        }
    }

    public void playerHit() {
        this.playerHand.add(deck.drawCard());
    }

    public void dealerHit() {
        this.dealerHand.add(deck.drawCard());
    }

    public void dealerMoves() {
        while (calculateHandValue(dealerHand) < 17) {
            dealerHit();
        }
    }

    public int calculateHandValue(@NotNull List<PlayingCard> hand) {
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

    public BlackjackStates checkWin(boolean updateDb) {
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
        try {
            blackjack = Objects.requireNonNull(blackjackDao.getByUserDiscordId(userDiscordId));
        } catch (SQLException e) {
            logger.error("Error getting blackjack entry from database when updating blackjack database.", e);
            return;
        }
        updateWallet(state);
        if (state.equals(BlackjackStates.DRAW)) {
            blackjack.addGame(database.model.Blackjack.BlackjackResult.DRAW, 0);
        } else if (state.equals(BlackjackStates.PLAYER_BLACKJACK) || state.equals(BlackjackStates.PLAYER_WIN)) {
            blackjack.addGame(database.model.Blackjack.BlackjackResult.WIN, this.winnings);
        } else {
            // it's a loss;
            blackjack.addGame(database.model.Blackjack.BlackjackResult.LOSS, -this.winnings);
        }
        try {
            blackjackDao.update(blackjack);
        } catch (SQLException e) {
            logger.error("Error updating blackjack entry in database when updating blackjack database.", e);
        }
    }

    public List<PlayingCard> getPlayerHand() {
        return playerHand;
    }

    public List<PlayingCard> getDealerHand() {
        return dealerHand;
    }

    public boolean isPlayerStand() {
        return playerStand;
    }

    public void setPlayerStand(boolean playerStand) {
        this.playerStand = playerStand;
    }

    public boolean isDealerStand() {
        return dealerStand;
    }

    public void setDealerStand(boolean dealerStand) {
        this.dealerStand = dealerStand;
    }

    public boolean isFinished() {
        return finished;
    }

    public void setFinished(boolean finished) {
        this.finished = finished;
    }

    public int getPlayerBet() {
        return playerBet;
    }

    public int getWinnings() {
        return winnings;
    }

    public long getStartTime() {
        return startTime;
    }

    public enum BlackjackStates {
        PLAYER_WIN,
        PLAYER_BLACKJACK,
        DEALER_WIN,
        DEALER_BLACKJACK,
        DRAW
    }
}
