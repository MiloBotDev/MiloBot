package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Emoji;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;
import utility.EmbedUtils;
import utility.GitHubBot;
import utility.Paginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays all issues labeled as a bug.
 */
public class BugListCmd extends Command implements SubCmd {

	private final GitHubBot gitHubBot;

	public BugListCmd() {
		this.commandName = "list";
		this.commandDescription = "Shows a list of all reported bugs.";

		this.gitHubBot = GitHubBot.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		ArrayList<EmbedBuilder> pages = createPages(event.getAuthor());
		EmbedBuilder startingEmbed = pages.get(0);
		EmbedUtils.styleEmbed(startingEmbed, event.getAuthor());
		Paginator paginator = new Paginator(startingEmbed);
		paginator.addPages(pages);
		String id = event.getAuthor().getId();
		event.getChannel().sendMessageEmbeds(startingEmbed.build()).setActionRows(ActionRow.of(
				Button.primary(id + ":previousPage", "Previous"),
				Button.secondary(id + ":delete", "Delete"),
				Button.primary(id + ":nextPage", "Next")
		)).queue(message -> paginator.initialize(message.getId()));
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandEvent event) {
		event.deferReply().queue();
		ArrayList<EmbedBuilder> pages = createPages(event.getUser());
		EmbedBuilder startingEmbed = pages.get(0);
		EmbedUtils.styleEmbed(startingEmbed, event.getUser());
		Paginator paginator = new Paginator(startingEmbed);
		paginator.addPages(pages);
		String id = event.getUser().getId();
		event.getHook().sendMessageEmbeds(startingEmbed.build()).addActionRows(ActionRow.of(
				Button.primary(id + ":previousPage", "Previous"),
				Button.secondary(id + ":delete", "Delete"),
				Button.primary(id + ":nextPage", "Next")
		)).queue(message -> paginator.initialize(message.getId()));
	}

	/**
	 * Creates all pages for the paginator.
	 */
	private @NotNull ArrayList<EmbedBuilder> createPages(User user) {
		ArrayList<EmbedBuilder> pages = new ArrayList<>();
		ArrayList<GHIssue> allBugs = gitHubBot.getAllBugs();
		StringBuilder description = new StringBuilder();

		EmbedBuilder page = new EmbedBuilder();
		EmbedUtils.styleEmbed(page, user);
		page.setTitle("Bugs");

		int rowCount = 0;
		for (int i = 0; i < allBugs.size(); i++) {
			GHIssue ghIssue = allBugs.get(i);
			description.append(String.format("`%s:` %s...\n", ghIssue.getNumber(), ghIssue.getTitle().substring(0, 50)));
			if (i + 1 == allBugs.size()) {
				page.setDescription(description.toString());
				pages.add(page);
				break;
			}
			rowCount++;
			if (rowCount == 10) {
				rowCount = 0;
				page.setDescription(description.toString());
				pages.add(page);
				page = new EmbedBuilder();
				EmbedUtils.styleEmbed(page, user);
				page.setTitle("Bugs");
				description = new StringBuilder();
			}
		}
		return pages;
	}
}
