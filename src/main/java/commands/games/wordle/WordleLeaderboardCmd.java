package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.WordleTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * View all the leaderboards for the Wordle command.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class WordleLeaderboardCmd extends Command implements SubCmd {

	private final DatabaseManager manager;

	public WordleLeaderboardCmd() {
		this.commandName = "leaderboard";
		this.commandDescription = "Check the wordle leaderboards.";
		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String consumerId = event.getAuthor().getId();
		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, event.getAuthor());
		embed.setTitle("Leaderboards");

		String description = "Select the leaderboard you want to view:\n" +
				":one: total games played.\n" +
				":two: highest streak.";
		embed.setDescription(description);

		event.getChannel().sendMessageEmbeds(embed.build()).queue(message -> {
			message.addReaction("1️⃣").queue();
			message.addReaction("2️⃣").queue();
			message.addReaction("⏹").queue();
			ListenerAdapter listener = getListenerAdapterLeaderboard(event, consumerId, message);
			message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener),
					1, TimeUnit.MINUTES);
			message.getJDA().addEventListener(listener);
		});

	}

	@NotNull
	private ListenerAdapter getListenerAdapterLeaderboard(@NotNull MessageReceivedEvent event, String consumerId, Message message) {
		return new ListenerAdapter() {
			@Override
			public void onMessageReactionAdd(@NotNull MessageReactionAddEvent eventReaction) {
				String messageId = eventReaction.getMessageId();
				if (Objects.requireNonNull(eventReaction.getUser()).getId().equals(consumerId)
						&& message.getId().equals(messageId)) {
					String asReactionCode = eventReaction.getReactionEmote().getAsReactionCode();
					ArrayList<EmbedBuilder> pages;
					switch (asReactionCode) {
						case "1️⃣":
							pages = makeLeaderboardEmbeds(event, "Top 100: total games played",
									WordleTableQueries.wordleGetTopTotalGamesPlayed);
							EmbedUtils.createPaginator(event, "Top 100: total games played", pages,
									message, consumerId);
							event.getJDA().removeEventListener(this);
							break;
						case "2️⃣":
							pages = makeLeaderboardEmbeds(event, "Top 100: highest streak",
									WordleTableQueries.wordleGetTopHighestStreak);
							EmbedUtils.createPaginator(event, "Top 100: highest streak", pages,
									message, consumerId);
							event.getJDA().removeEventListener(this);
							break;
						case "⏹":
							event.getJDA().removeEventListener(this);
							event.getChannel().deleteMessageById(messageId).queue();
					}

				}
			}


			private @NotNull ArrayList<EmbedBuilder> makeLeaderboardEmbeds(MessageReceivedEvent event, String title, String query) {
				ArrayList<EmbedBuilder> embedPages = new ArrayList<>();
				ArrayList<String> result = manager.query(query, DatabaseManager.QueryTypes.RETURN);
				int rowCount = 0;
				int rank = 1;
				EmbedBuilder page = new EmbedBuilder();
				page.setTitle(title);
				EmbedUtils.styleEmbed(page, event.getAuthor());
				StringBuilder description = new StringBuilder();
				for (int i = 0; i < result.size(); i += 2) {
					description.append(String.format("`%d`: %s - %s total games.\n", rank, result.get(i), result.get(i + 1)));
					if (i + 2 == result.size()) {
						page.setDescription(description.toString());
						embedPages.add(page);
						break;
					}
					rowCount++;
					if (rowCount == 15) {
						page.setDescription(description.toString());
						embedPages.add(page);

						rowCount = 0;
						page = new EmbedBuilder();
						EmbedUtils.styleEmbed(page, event.getAuthor());
						page.setTitle(title);
						description = new StringBuilder();
					}
					rank++;
				}
				return embedPages;
			}
		};
	}
}
