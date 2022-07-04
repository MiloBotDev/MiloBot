package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;
import utility.EmbedUtils;
import utility.GitHubBot;

import java.util.List;
import java.util.Optional;

public class BugViewCmd extends Command implements SubCmd {

	private final GitHubBot gitHubBot;

	public BugViewCmd() {
		this.commandName = "view";
		this.commandDescription = "Lookup a specific bug on the issue tracker.";

		this.gitHubBot = GitHubBot.getInstance();
	}


	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if (args.size() < 1) {
			sendCommandUsage(event, this.commandName, this.commandArgs);
		} else {
			Optional<GHIssue> bug = gitHubBot.getBug(Integer.parseInt(args.get(0)));
			if (bug.isEmpty()) {
				event.getChannel().sendMessage(String.format("Bug with number: %s not found.", args.get(0))).queue();
			} else {
				GHIssue ghIssue = bug.get();
				EmbedBuilder embed = new EmbedBuilder();
				EmbedUtils.styleEmbed(embed, event.getAuthor());
				embed.setTitle(ghIssue.getTitle());
				String body = ghIssue.getBody().replaceAll("#", "")
						.replaceAll("Steps to Reproduce", "**Steps to Reproduce:**")
						.replaceAll("Severity", "**Severity:**")
						.replaceAll("Additional Information", "**Additional Information:**")
						.replaceAll("Author", "**Author:**");
				embed.setDescription(body);
				event.getChannel().sendMessageEmbeds(embed.build()).queue(EmbedUtils.deleteEmbedButton(event,
						event.getAuthor().getId()));
			}
		}
	}
}
