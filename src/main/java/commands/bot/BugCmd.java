package commands.bot;

import commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.GitHubBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class BugCmd extends Command implements BotCmd {

	private final ArrayList<String> questions;
	private final GitHubBot gitHubBot;

	public BugCmd() {
		this.commandName = "bug";
		this.commandDescription = "Report a bug you found.";
		this.cooldown = 120;

		this.questions = new ArrayList<>(List.of(new String[]{
				"Please give me a short summary of the bug you found. You can type cancel at any time to stop the command.",
				"How do you reproduce the bug?",
				"On a scale of 1-5, how severe would you say the bug is?",
				"Do you have any additional information about this bug?",
		}));
		this.gitHubBot = GitHubBot.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String authorId = event.getAuthor().getId();
		ArrayList<String> results = new ArrayList<>();
		askQuestion(event, this.questions, authorId, results);
	}

	/**
	 * Asks the next question for the bug report. Calls itself recursively till all questions have been asked.
	 */
	private void askQuestion(@NotNull MessageReceivedEvent event, ArrayList<String> questions, String authorId,
							 @NotNull ArrayList<String> results) {
		if(results.size() == 4) {
			gitHubBot.createBugIssue(results.get(0), results.get(1), results.get(2), results.get(3),
					event.getAuthor().getName(), authorId);
			event.getChannel().sendMessage("Your bug report has been submitted.").queue();
		} else {
			event.getChannel().sendMessage(questions.get(results.size())).queue(message -> {
				ListenerAdapter listener = new ListenerAdapter() {
					@Override
					public void onMessageReceived(@NotNull MessageReceivedEvent event) {
						if(event.getAuthor().getId().equals(authorId)) {
							event.getJDA().removeEventListener(this);
							if(event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).equals("cancel")) {
								event.getChannel().sendMessage("Bug report canceled.").queue();
							} else {
								results.add(event.getMessage().getContentRaw());
								askQuestion(event, questions, authorId, results);
							}
						}
					}
				};
				message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener),
						2, TimeUnit.MINUTES);
				message.getJDA().addEventListener(listener);
			});
		}
	}
}
