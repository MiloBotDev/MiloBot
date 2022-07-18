package games;

import database.DatabaseManager;
import database.queries.BlackjackTableQueries;
import database.queries.UserTableQueries;
import models.BlackjackStates;
import models.CardDeck;
import models.PlayingCards;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Blackjack {

	private final List<PlayingCards> playerHand;
	private final List<PlayingCards> dealerHand;
	private final CardDeck deck;
	private final DatabaseManager dbManager;
	private final int playerBet;
	private final String userId;

	private boolean playerStand;
	private boolean dealerStand;
	private boolean finished;
	private int winnings;
	private final long startTime;

	public Blackjack(String userId) {
		this.playerHand = new ArrayList<>();
		this.dealerHand = new ArrayList<>();
		this.deck = new CardDeck();
		this.dbManager = DatabaseManager.getInstance();
		this.userId = userId;
		this.playerBet = 0;
		this.winnings = 0;
		this.startTime = System.nanoTime();
	}

	public Blackjack(String userId, int bet) {
		this.playerHand = new ArrayList<>();
		this.dealerHand = new ArrayList<>();
		this.deck = new CardDeck();
		this.dbManager = DatabaseManager.getInstance();
		this.userId = userId;
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
		ArrayList<String> query = dbManager.query(UserTableQueries.getUserCurrency, DatabaseManager.QueryTypes.RETURN, userId);
		int playerWallet = Integer.parseInt(query.get(0));
		int newWallet;
		if(state == null) {
			newWallet = playerWallet - playerBet;
		} else {
			if(state.equals(BlackjackStates.PLAYER_BLACKJACK)) {
				this.winnings = (int) Math.ceil(((double) playerBet) * 1.5d);
				newWallet = playerWallet  + (int) Math.ceil(((double) playerBet) * 2.5d);
			} else if(state.equals(BlackjackStates.PLAYER_WIN)) {
				this.winnings = playerBet;
				newWallet = playerWallet + (winnings * 2);
			} else if(state.equals(BlackjackStates.DRAW)) {
				this.winnings = playerBet;
				newWallet = playerWallet + playerBet;
			} else {
				this.winnings = playerBet;
				newWallet = playerWallet;
			}
		}
		dbManager.query(UserTableQueries.updateUserCurrency, DatabaseManager.QueryTypes.UPDATE, String.valueOf(newWallet), userId);
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

	public int calculateHandValue(@NotNull List<PlayingCards> hand) {
		int total = 0;
		ArrayList<PlayingCards> aces = new ArrayList<>();
		for(PlayingCards card : hand) {
			total += card.getValue();
			if(card.equals(PlayingCards.ACE_OF_SPADES) || card.equals(PlayingCards.ACE_OF_HEARTS) ||
					card.equals(PlayingCards.ACE_OF_DIAMONDS) || card.equals(PlayingCards.ACE_OF_CLUBS)) {
				aces.add(card);
			}
		}
		// TODO: replace with a simple int since were not accessing the list
		for(PlayingCards ace : aces) {
			if(total > 21) {
				total -= 10;
			}
		}
		return total;
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
		if(updateDb) {
			updateBlackjackDatabase(state);
		}
		return state;
	}

	private void updateBlackjackDatabase(BlackjackStates state) {
		ArrayList<String> query = dbManager.query(BlackjackTableQueries.getUser, DatabaseManager.QueryTypes.RETURN, userId);
		String wonLastGame = query.get(1);
		String streak = query.get(2);
		String totalGames = query.get(3);
		String totalWins = query.get(4);
		String totalEarnings = query.get(5);
		String totalDraws = query.get(6);
		String highestStreak = query.get(7);
		String newTotalGames= String.valueOf(Integer.parseInt(totalGames) + 1);
		String newTotalDraws = String.valueOf(Integer.parseInt(totalDraws) + 1);
		String newTotalWins = String.valueOf(Integer.parseInt(totalWins) + 1);
		String newStreak = String.valueOf(Integer.parseInt(streak) + 1);
		String newHighestStreak;
		if(Integer.parseInt(newStreak) > Integer.parseInt(highestStreak)) {
			newHighestStreak = newStreak;
		} else {
			newHighestStreak = highestStreak;
		}
		updateWallet(state);
		if(state.equals(BlackjackStates.DRAW)) {
			dbManager.query(BlackjackTableQueries.updateUserDraw, DatabaseManager.QueryTypes.UPDATE,
					newTotalGames, newTotalDraws, String.valueOf(Integer.parseInt(totalEarnings)), userId);
		} else if(state.equals(BlackjackStates.PLAYER_BLACKJACK)) {
			dbManager.query(BlackjackTableQueries.updateUserWin, DatabaseManager.QueryTypes.UPDATE,
					newStreak, newTotalGames, newTotalWins, String.valueOf(Integer.parseInt(totalEarnings) + this.winnings),
					newHighestStreak, userId);
		} else if(state.equals(BlackjackStates.PLAYER_WIN)) {
			dbManager.query(BlackjackTableQueries.updateUserWin, DatabaseManager.QueryTypes.UPDATE,
					newStreak, newTotalGames, newTotalWins, String.valueOf(Integer.parseInt(totalEarnings) + this.winnings),
					newHighestStreak, userId);
		} else {
			// it's a loss;
			dbManager.query(BlackjackTableQueries.updateUserLoss, DatabaseManager.QueryTypes.UPDATE,
					newTotalGames, String.valueOf(Integer.parseInt(totalEarnings) - this.winnings), userId);
		}
	}

	public List<PlayingCards> getPlayerHand() {
		return playerHand;
	}

	public List<PlayingCards> getDealerHand() {
		return dealerHand;
	}

	public boolean isPlayerStand() {
		return playerStand;
	}

	public boolean isDealerStand() {
		return dealerStand;
	}

	public boolean isFinished() {
		return finished;
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

	public void setPlayerStand(boolean playerStand) {
		this.playerStand = playerStand;
	}

	public void setDealerStand(boolean dealerStand) {
		this.dealerStand = dealerStand;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}
}
