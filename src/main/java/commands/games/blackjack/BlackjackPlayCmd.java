package commands.games.blackjack;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.BlackjackTableQueries;
import database.queries.UserTableQueries;
import games.Blackjack;
import models.BlackjackStates;
import models.PlayingCards;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
					event.getChannel().sendMessage("You can't bet a negative amount of morbcoins.").queue();
					return;
				} else if(playerBet == 0) {
					event.getChannel().sendMessage("You can't bet `0` morbcoins.").queue();
					return;
				} else {
					ArrayList<String> query = dbManager.query(UserTableQueries.getUserCurrency, DatabaseManager.QueryTypes.RETURN, authorId);
					int playerWallet = Integer.parseInt(query.get(0));
					if(playerBet > playerWallet) {
						event.getChannel().sendMessage(String.format("You can't bet `%d` morbcoins, you only have `%d` in your wallet.", playerBet, playerWallet)).queue();
						return;
					} else {
						bet = playerBet;
					}
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
			blackjackStates = blackJack.checkWin(true);
			embed = generateBlackjackEmbed(event.getAuthor(), blackjackStates);
		} else {
			embed = generateBlackjackEmbed(event.getAuthor(), null);
		}
		event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
				Button.primary(authorId + ":stand", "Stand"),
				Button.primary(authorId + ":hit", "Hit")
		)).queue();
	}

	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
	}

	public static @NotNull EmbedBuilder generateBlackjackEmbed(@NotNull User user, BlackjackStates state) {
		Blackjack game = blackjackGames.get(user.getId());

		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, user);
		embed.setTitle("Blackjack");

		if(game.getPlayerBet() > 0) {
			embed.setDescription("You have bet `" + game.getPlayerBet() + "` morbcoins.");
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
					embed.addField("------------", "**Dealer Wins!**", false);
					game.setFinished(true);
				}
			} else {
				if(state.equals(BlackjackStates.PLAYER_WIN)) {
					embed.addField("------------", String.format("**%s** wins!\nYou win `%d` morbcoins.",
							user.getName(), game.getWinnings()), false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.DRAW)) {
					embed.addField("------------", "Its a draw!\nYou lose nothing.", false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.DEALER_WIN)) {
					embed.addField("------------", String.format("Dealer wins!\nYou lose `%d` morbcoins.",
							game.getWinnings()), false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.DEALER_BLACKJACK)) {
					embed.addField("------------", String.format("Dealer wins with blackjack!\nYou lose `%d` morbcoins.",
							game.getWinnings()), false);
					game.setFinished(true);
				} else if(state.equals(BlackjackStates.PLAYER_BLACKJACK)) {
					embed.addField("------------", String.format("**%s** wins with blackjack!\nYou win `%d` morbcoins.",
							user.getName(), game.getWinnings()), false);
					game.setFinished(true);
				}
			}

		}
		return embed;
	}
}
