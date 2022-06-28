package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;
import utility.EmbedUtils;
import utility.GitHubBot;

import java.util.ArrayList;
import java.util.List;

/**
 * Displays all issues labeled as a bug.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class BugListCmd extends Command implements SubCmd {

	private final GitHubBot gitHubBot;

	public BugListCmd() {
		this.commandName = "list";
		this.commandDescription = "Shows a list of all reported and unfinished bugs.";

		this.gitHubBot = GitHubBot.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		ArrayList<EmbedBuilder> pages = createPages();

		EmbedBuilder startingEmbed = pages.get(0);
		EmbedUtils.styleEmbed(event, startingEmbed);
		event.getChannel().sendMessageEmbeds(startingEmbed.build()).queue(message ->
				EmbedUtils.createPaginator(event, "Bugs", pages, message, event.getAuthor().getId()));
	}

	/**
	 * Creates all pages for the paginator.
	 *
	 * @return The pages in an ArrayList<EmbedBuilder>.
	 */
	private @NotNull ArrayList<EmbedBuilder> createPages() {
		ArrayList<EmbedBuilder> pages = new ArrayList<>();
		ArrayList<GHIssue> allBugs = gitHubBot.getAllBugs();
		StringBuilder description = new StringBuilder();

		EmbedBuilder page = new EmbedBuilder();
		page.setTitle("Bugs");

		int rowCount = 0;
		for(int i=0; i < allBugs.size(); i++) {
			GHIssue ghIssue = allBugs.get(i);
			description.append(String.format("`%s:` %s...\n", ghIssue.getNumber(), ghIssue.getTitle().substring(0, 50)));
			if(i + 1 == allBugs.size()) {
				page.setDescription(description.toString());
				pages.add(page);
				break;
			}
			rowCount++;
			if(rowCount == 10) {
				rowCount = 0;
				page.setDescription(description.toString());
				pages.add(page);
				page = new EmbedBuilder();
				page.setTitle("Bugs");
				description = new StringBuilder();
			}
		}
		return pages;
	}
}
