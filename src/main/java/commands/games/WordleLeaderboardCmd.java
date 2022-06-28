package commands.games;

import commands.Command;
import commands.SubCommand;
import database.DatabaseManager;
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

public class WordleLeaderboardCmd extends Command implements SubCommand {

	private final DatabaseManager manager;

	public WordleLeaderboardCmd() {
		this.commandName = "leaderboard";
		this.commandDescription = "Check the wordle leaderboards.";
		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String consumerName = event.getAuthor().getName();
		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(event, embed);
		embed.setTitle("Leaderboards");

		String description = "Select the leaderboard you want to view:\n" +
				":one: total games played.\n" +
				":two: highest streak.";
		embed.setDescription(description);

		event.getChannel().sendMessageEmbeds(embed.build()).queue(message -> {
			message.addReaction("1️⃣").queue();
			message.addReaction("2️⃣").queue();
			message.addReaction("❌").queue();
			ListenerAdapter listener = getListenerAdapterLeaderboard(event, consumerName, message);
			message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener),
					1, TimeUnit.MINUTES);
			message.getJDA().addEventListener(listener);
		});

	}

	@NotNull
	private ListenerAdapter getListenerAdapterLeaderboard(@NotNull MessageReceivedEvent event, String consumerName, Message message) {
		return new ListenerAdapter() {
			@Override
			public void onMessageReactionAdd(@NotNull MessageReactionAddEvent eventReaction) {
				String messageId = eventReaction.getMessageId();
				if (Objects.requireNonNull(eventReaction.getUser()).getName().equals(consumerName)
						&& message.getId().equals(messageId)) {
					String asReactionCode = eventReaction.getReactionEmote().getAsReactionCode();
					switch (asReactionCode) {
						case "1️⃣":
							createLeaderBoardEmbed(messageId, "Top 100: total games played", manager.wordleGetTopTotalGamesPlayed);
							break;
						case "2️⃣":
							createLeaderBoardEmbed(messageId, "Top 100: highest streak", manager.wordleGetTopHighestStreak);
							break;
						case "❌":
							event.getJDA().removeEventListener(this);
							event.getChannel().deleteMessageById(messageId).queue();
					}

				}
			}

			private void createLeaderBoardEmbed(String messageId, String title, String query) {
				message.clearReactions().queue();
				ArrayList<EmbedBuilder> pages = makeLeaderboardEmbeds(event, title,
						query);
				EmbedBuilder embedBuilder = pages.get(0);
				message.editMessageEmbeds(embedBuilder.build()).queue(message1 -> {
							final int[] currentPage = {0};
							if (pages.size() != 0) {
								message.addReaction("◀").queue();
								message.addReaction("▶").queue();
							}
							message.addReaction("❌").queue();
							ListenerAdapter totalGames = new ListenerAdapter() {
								@Override
								public void onMessageReactionAdd(@NotNull MessageReactionAddEvent eventReaction2) {
									if (Objects.requireNonNull(eventReaction2.getUser()).getName().equals(consumerName)
											&& message.getId().equals(messageId)) {
										String asReactionCode = eventReaction2.getReactionEmote().getAsReactionCode();
										EmbedBuilder newEmbed = new EmbedBuilder();
										newEmbed.setTitle(title);
										EmbedUtils.styleEmbed(event, newEmbed);
										switch (asReactionCode) {
											case "◀":
												message.removeReaction(asReactionCode, eventReaction2.getUser()).queue();
												if (!(currentPage[0] - 1 < 0)) {
													currentPage[0]--;
													newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
													message.editMessageEmbeds(newEmbed.build()).queue();
												}
												break;
											case "▶":
												message.removeReaction(asReactionCode, eventReaction2.getUser()).queue();
												if (!(currentPage[0] + 1 == pages.size())) {
													currentPage[0]++;
													newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
													message.editMessageEmbeds(newEmbed.build()).queue();
												}
												break;
											case "❌":
												event.getJDA().removeEventListener(this);
												event.getChannel().deleteMessageById(messageId).queue();
												break;
										}
									}
								}
							};
							message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(totalGames),
									2, TimeUnit.MINUTES);
							message.getJDA().addEventListener(totalGames);
						}
				);
				event.getJDA().removeEventListener(this);
			}
		};
	}

	private @NotNull ArrayList<EmbedBuilder> makeLeaderboardEmbeds(MessageReceivedEvent event, String title, String query) {
		ArrayList<EmbedBuilder> embedPages = new ArrayList<>();
		ArrayList<String> result = this.manager.query(query, DatabaseManager.QueryTypes.RETURN);
		int rowCount = 0;
		int rank = 1;
		EmbedBuilder page = new EmbedBuilder();
		page.setTitle(title);
		EmbedUtils.styleEmbed(event, page);
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
				EmbedUtils.styleEmbed(event, page);
				page.setTitle(title);
				description = new StringBuilder();
			}
			rank++;
		}
		return embedPages;
	}

}
