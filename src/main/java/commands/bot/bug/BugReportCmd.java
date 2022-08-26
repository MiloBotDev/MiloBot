package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.GitHubBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Report a bug you have found. The bug will be added to the issue tracker on the repository.
 */
public class BugReportCmd extends Command implements SubCmd {

    private final ArrayList<String> questions;
    private final GitHubBot gitHubBot;

    public BugReportCmd() {
        this.commandName = "report";
        this.commandDescription = "Report a bug you found.";
        this.cooldown = 0;

        this.questions = new ArrayList<>(List.of(new String[]{
                "Please give me a short summary of the bug you found. You can type cancel at any time to stop the command.",
                "How do you reproduce the bug?",
                "On a scale of 1-5, how severe would you say the bug is?",
                "Do you have any additional information about this bug?",
        }));
        this.gitHubBot = GitHubBot.getInstance();
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        ArrayList<String> results = new ArrayList<>();
        askQuestion(event.getJDA(), event.getChannel(), event.getAuthor(), this.questions, results, null);
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        ArrayList<String> results = new ArrayList<>();
        askQuestion(event.getJDA(), event.getChannel(), event.getUser(), this.questions, results, event);
    }

    /**
     * Asks the next question for the bug report. Calls itself recursively till all questions have been asked.
     */
    private void askQuestion(JDA jda, MessageChannel channel, User author, ArrayList<String> questions,
                             @NotNull ArrayList<String> results, SlashCommandEvent slashEvent) {
        if (results.size() == 4) {
            String bugIssue = gitHubBot.createBugIssue(results.get(0), results.get(1), results.get(2), results.get(3),
                    author.getName(), author.getId());
            String format = String.format("Your bug report has been submitted. " +
                    "You can view your submitted bug here: %s", bugIssue);
            if (slashEvent != null) {
                slashEvent.getHook().sendMessage(format).queue();
            } else {
                channel.sendMessage(format).queue();
            }
        } else {
            channel.sendMessage(questions.get(results.size())).queue(message -> {
                ListenerAdapter listener = new ListenerAdapter() {
                    @Override
                    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                        if (event.getChannelType() != ChannelType.TEXT) {
                            return;
                        }

                        if (event.getAuthor().getId().equals(author.getId())) {
                            event.getJDA().removeEventListener(this);
                            if (event.getMessage().getContentRaw().toLowerCase(Locale.ROOT).equals("cancel")) {
                                if (slashEvent != null) {
                                    slashEvent.getHook().sendMessage("Bug report canceled.").queue();
                                } else {
                                    channel.sendMessage("Bug report canceled.").queue();
                                }
                            } else {
                                results.add(event.getMessage().getContentRaw());
                                askQuestion(jda, channel, author, questions, results, slashEvent);
                            }
                        }
                    }
                };

                message.getJDA().getRateLimitPool().schedule(() -> {
                    if (jda.getRegisteredListeners().contains(listener)) {
                        jda.removeEventListener(listener);
                        if (slashEvent != null) {
                            slashEvent.getHook().sendMessage("Timed out!").queue();
                        } else {
                            channel.sendMessage("Timed out!").queue();
                        }
                    }
                }, 2, TimeUnit.MINUTES);
                message.getJDA().addEventListener(listener);
            });
        }
    }
}
