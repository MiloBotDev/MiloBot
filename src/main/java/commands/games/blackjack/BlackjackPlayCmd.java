package commands.games.blackjack;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.BlackjackTableQueries;
import database.queries.UsersTableQueries;
import games.Blackjack;
import models.BlackjackStates;
import models.cards.PlayingCards;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.math.BigInteger;
import java.util.*;

public class BlackjackPlayCmd extends Command implements SubCmd {

	public static Map<String, Blackjack> blackjackGames = new HashMap<>();

	private final DatabaseManager dbManager;

	public BlackjackPlayCmd() {
		this.commandName = "play";
		this.commandDescription = "Play a game of blackjack on discord.";
		this.commandArgs = new String[]{"bet*"};
		this.dbManager = DatabaseManager.getInstance();
	}

	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String authorId = event.getAuthor().getId();

		int bet = 0;
		if(args.size() > 0) {
			try {
				int playerBet = Integer.parseInt(args.get(0));
				if(playerBet < 0) {
					event.getChannel().sendMessage("You can't bet a negative amount of Morbcoins.").queue();
					return;
				} else if(playerBet == 0) {
					event.getChannel().sendMessage("You can't bet `0` Morbcoins.").queue();
					return;
				} else if (playerBet > 10000) {
					event.getChannel().sendMessage("You can't bet more than `10000` Morbcoins.").queue();
					return;
				} else {
					ArrayList<String> query = dbManager.query(UsersTableQueries.getUserCurrency, DatabaseManager.QueryTypes.RETURN, authorId);
					BigInteger playerWallet = new BigInteger(query.get(0));
					char c = playerWallet.subtract(BigInteger.valueOf(playerBet)).toString().toCharArray()[0];
					try {
						int integer = Integer.parseInt(String.valueOf(c));
						if(integer < 0) {
							event.getChannel().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", playerBet, playerWallet)).queue();							return;
						}
					} catch (NumberFormatException e) {
						event.getChannel().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", playerBet, playerWallet)).queue();
						return;
					}
					bet = playerBet;
				}
			} catch(NumberFormatException e) {
				event.getChannel().sendMessage("Invalid bet amount.").queue();
				return;
			}
		}

		ArrayList<String> result = dbManager.query(BlackjackTableQueries.checkIfUserExists, DatabaseManager.QueryTypes.RETURN, authorId);
		if(result.size() == 0) {
			dbManager.query(BlackjackTableQueries.addUser, DatabaseManager.QueryTypes.UPDATE, authorId);
		}
		if(blackjackGames.containsKey(authorId)) {
			event.getChannel().sendMessage("You are already in a game of blackjack.").queue();
			return;
		}

		Blackjack blackJack = new Blackjack(authorId, bet);
		blackJack.initializeGame();

		blackjackGames.put(authorId, blackJack);

		BlackjackStates blackjackStates = blackJack.checkWin(false);
		EmbedBuilder embed;
		if(blackjackStates.equals(BlackjackStates.PLAYER_BLACKJACK)) {
			blackJack.dealerHit();
			blackJack.setDealerStand(true);
			blackjackStates = blackJack.checkWin(true);
			embed = generateBlackjackEmbed(event.getAuthor(), blackjackStates);
			BlackjackPlayCmd.blackjackGames.remove(authorId);
			event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
					Button.primary(authorId + ":replayBlackjack", "Replay"),
					Button.secondary(authorId + ":delete", "Delete")
			)).queue();
		} else {
			embed = generateBlackjackEmbed(event.getAuthor(), null);
			event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
					Button.primary(authorId + ":stand", "Stand"),
					Button.primary(authorId + ":hit", "Hit")
			)).queue();
		}
	}

	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		event.deferReply().queue();
		String authorId = event.getUser().getId();
		int bet;
		if(event.getOption("bet") == null) {
			bet = 0;
		} else {
			bet = Objects.requireNonNull(event.getOption("bet")).getAsInt();
			ArrayList<String> query = dbManager.query(UsersTableQueries.getUserCurrency, DatabaseManager.QueryTypes.RETURN, authorId);
			BigInteger playerWallet = new BigInteger(query.get(0));
			char c = playerWallet.subtract(BigInteger.valueOf(bet)).toString().toCharArray()[0];
			try {
				int integer = Integer.parseInt(String.valueOf(c));
				if(integer < 0) {
					event.getHook().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();							return;
				}
			} catch (NumberFormatException e) {
				event.getHook().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
				return;
			}
		}
		ArrayList<String> result = dbManager.query(BlackjackTableQueries.checkIfUserExists, DatabaseManager.QueryTypes.RETURN, authorId);
		if(result.size() == 0) {
			dbManager.query(BlackjackTableQueries.addUser, DatabaseManager.QueryTypes.UPDATE, authorId);
		}
		if(blackjackGames.containsKey(authorId)) {
			event.getHook().sendMessage("You are already in a game of blackjack.").queue();
			return;
		}

		Blackjack blackJack = new Blackjack(authorId, bet);
		blackJack.initializeGame();

		blackjackGames.put(authorId, blackJack);

		BlackjackStates blackjackStates = blackJack.checkWin(false);
		EmbedBuilder embed;
		if(blackjackStates.equals(BlackjackStates.PLAYER_BLACKJACK)) {
			blackJack.dealerHit();
			blackJack.setDealerStand(true);
			blackjackStates = blackJack.checkWin(true);
			embed = generateBlackjackEmbed(event.getUser(), blackjackStates);
			BlackjackPlayCmd.blackjackGames.remove(authorId);
			event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
					Button.primary(authorId + ":replayBlackjack", "Replay"),
					Button.secondary(authorId + ":delete", "Delete")
			)).queue();
		} else {
			embed = generateBlackjackEmbed(event.getUser(), null);
			event.getHook().sendMessageEmbeds(embed.build()).addActionRows(ActionRow.of(
					Button.primary(authorId + ":stand", "Stand"),
					Button.primary(authorId + ":hit", "Hit")
			)).queue();
		}
	}

	public static @NotNull EmbedBuilder generateBlackjackEmbed(@NotNull User user, BlackjackStates state) {
		Blackjack game = blackjackGames.get(user.getId());

		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, user);
		embed.setTitle("Blackjack");

		if(game.getPlayerBet() > 0) {
			embed.setDescription("You have bet `" + game.getPlayerBet() + "` Morbcoins.");
		}

		embed.addField("------------", "**Dealer Hand**", false);
		List<PlayingCards> dealerHand = game.getDealerHand();
		for(int i = 0; i < dealerHand.size(); i++) {
			embed.addField(String.format("Card %d", i + 1), dealerHand.get(i).getLabel(), true);
		}
		embed.addField("Total", String.format("%d", game.calculateHandValue(dealerHand)), false);

		embed.addField("------------", "**Player Hand**", false);
		List<PlayingCards> playerHand = game.getPlayerHand();
		for(int i = 0; i < playerHand.size(); i++) {
			embed.addField(String.format("Card %d", i + 1), playerHand.get(i).getLabel(), true);
		}
		embed.addField("Total", String.format("%d", game.calculateHandValue(playerHand)), false);

		if(state != null) {
			if(!game.isDealerStand()) {
				if(state.equals(BlackjackStates.DEALER_WIN)) {
					String value = "**Dealer Wins!**\n";
					if(game.getPlayerBet() > 0) {
						value += String.format("You lose `%d` Morbcoins!\n", game.getWinnings());
					}
					embed.addField("------------", value, false);
					game.setFinished(true);
				}
			} else {
				if(state.equals(BlackjackStates.PLAYER_WIN)) {
					String format = String.format("**%s** wins!\n", user.getName());
					if(game.getPlayerBet() > 0) {
						format += String.format("You win `%d` Morbcoins!", game.getWinnings());
					}
					embed.addField("------------", format, false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.DRAW)) {
					String value = "Its a draw!\n";
					if(game.getPlayerBet() > 0) {
						value += "You lose nothing.";
					}
					embed.addField("------------", value, false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.DEALER_WIN)) {
					String format = "Dealer wins!\n";
					if(game.getPlayerBet() > 0) {
						format += String.format("You lose `%d` Morbcoins!", game.getWinnings());
					}
					embed.addField("------------", format, false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.DEALER_BLACKJACK)) {
					String format = "Dealer wins with blackjack!\n";
					if(game.getPlayerBet() > 0) {
						format += String.format("You lose `%d` Morbcoins!", game.getWinnings());
					}
					embed.addField("------------", format, false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.PLAYER_BLACKJACK)) {
					String format = String.format("**%s** wins with blackjack!\n", user.getName());
					if(game.getPlayerBet() > 0) {
						format += String.format("You win `%d` Morbcoins!", game.getWinnings());
					}
					embed.addField("------------", format, false);
					game.setFinished(true);
				}
			}

		}
		return embed;
	}
}
