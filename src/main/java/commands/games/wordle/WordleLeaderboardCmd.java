package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * View all the leaderboards for the Wordle command.
 */
public class WordleLeaderboardCmd extends Command implements SubCmd {

	private static final DatabaseManager manager = DatabaseManager.getInstance();;

	public WordleLeaderboardCmd() {
		this.commandName = "leaderboard";
		this.commandDescription = "View the wordle leaderboards.";
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		EmbedBuilder embed = new EmbedBuilder();
		User author = event.getAuthor();
		EmbedUtils.styleEmbed(embed, author);
		embed.setTitle("Leaderboards");

		String description = "Select the leaderboard you want to view:\n" +
				":one: total games played.\n" +
				":two: highest streak. \n" +
				":three: current streak.";
		embed.setDescription(description);

		String id = author.getId();
		event.getChannel().sendMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
				Button.primary(id + ":totalGamesPlayed", Emoji.fromMarkdown("1️⃣")),
				Button.primary(id + ":highestStreak", Emoji.fromMarkdown("2️⃣")),
				Button.primary(id + ":currentStreak", Emoji.fromMarkdown("3️⃣")),
				Button.secondary(id + ":delete", "Delete")
		)).queue();
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		EmbedBuilder embed = new EmbedBuilder();
		User user = event.getUser();
		EmbedUtils.styleEmbed(embed, user);
		embed.setTitle("Leaderboards");

		String description = "Select the leaderboard you want to view:\n" +
				":one: total games played.\n" +
				":two: highest streak.";
		embed.setDescription(description);

		String id = user.getId();
		event.replyEmbeds(embed.build()).addActionRows(ActionRow.of(
				Button.primary(id + ":totalGamesPlayed", Emoji.fromMarkdown("1️⃣")),
				Button.primary(id + ":highestStreak", Emoji.fromMarkdown("2️⃣")),
				Button.primary(id + ":currentStreak", Emoji.fromMarkdown("3️⃣")),
				Button.secondary(id + ":delete", "Delete")
		)).queue();
	}

	public static @NotNull ArrayList<EmbedBuilder> makeLeaderboardEmbeds(User author, String title, String query) {
		ArrayList<EmbedBuilder> embedPages = new ArrayList<>();
		ArrayList<String> result = manager.query(query, DatabaseManager.QueryTypes.RETURN);
		int rowCount = 0;
		int rank = 1;
		EmbedBuilder page = new EmbedBuilder();
		page.setTitle(title);
		EmbedUtils.styleEmbed(page, author);
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
				EmbedUtils.styleEmbed(page, author);
				page.setTitle(title);
				description = new StringBuilder();
			}
			rank++;
		}
		return embedPages;
	}

}

