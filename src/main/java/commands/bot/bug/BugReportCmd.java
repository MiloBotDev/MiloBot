package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import utility.Config;
import utility.GitHubBot;

import java.util.*;
import java.util.concurrent.*;

/**
 * Report a bug you have found. The bug will be added to the issue tracker on the repository.
 */
public class BugReportCmd extends Command implements SubCmd {

    private final ArrayList<String> questions;
    private final GitHubBot gitHubBot;
    private final ScheduledExecutorService idleInstanceCleanupExecutorService = Executors.newScheduledThreadPool(1);
    private class UserBugReportInstance {
        private UserBugReportInstance(User user) {
            this.user = user;
        }
        private int nextQuestion = 0;
        private final User user;
        private final String[] responses = new String[questions.size()];
        private ScheduledFuture<?> idleInstanceCleanupFuture;
        private boolean cancelIdleInstanceCleanup() {
            return idleInstanceCleanupFuture.cancel(false);
        }

        private void setIdleInstanceCleanup() {
            idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
                bugReportInstances.remove(user.getIdLong());
                user.openPrivateChannel().queue(privateChannel -> privateChannel
                        .sendMessage("You have been idle for too long. Your bug report has been cancelled.").queue());
            }, 15, TimeUnit.MINUTES);
        }
    }
    private final Map<Long, UserBugReportInstance> bugReportInstances = new ConcurrentHashMap<>();

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
        this.listeners.add(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                onMessage(event);
            }
        });
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (bugReportInstances.containsKey(event.getAuthor().getIdLong())) {
            event.getChannel().sendMessage("You already have an active bug report instance.").queue();
            return;
        }
        UserBugReportInstance instance = new UserBugReportInstance(event.getAuthor());
        event.getAuthor().openPrivateChannel().queue(ch ->
                ch.sendMessage(this.questions.get(instance.nextQuestion)).queue());
        instance.setIdleInstanceCleanup();
        bugReportInstances.put(event.getAuthor().getIdLong(), instance);
        event.getChannel().sendMessage("Please continue the bug report in the DM with Milobot.").queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        if (bugReportInstances.containsKey(event.getUser().getIdLong())) {
            event.reply("You already have an active bug report instance.").queue();
            return;
        }
        UserBugReportInstance instance = new UserBugReportInstance(event.getUser());
        event.getUser().openPrivateChannel().queue(ch ->
                ch.sendMessage(this.questions.get(instance.nextQuestion)).queue());
        instance.setIdleInstanceCleanup();
        bugReportInstances.put(event.getUser().getIdLong(), instance);
        event.reply("Please continue the bug report in the DM with Milobot.").queue();
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(Config.getInstance().getPrivateChannelPrefix()) ||
                event.getChannelType() != ChannelType.PRIVATE || event.getAuthor().isBot()) {
            return;
        }
        UserBugReportInstance instance = bugReportInstances.get(event.getAuthor().getIdLong());
        if (instance == null || !instance.cancelIdleInstanceCleanup()) {
            return;
        }
        if (event.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
            event.getChannel().sendMessage("Cancelled bug report.").queue();
            bugReportInstances.remove(event.getAuthor().getIdLong());
            return;
        }
        instance.responses[instance.nextQuestion] = event.getMessage().getContentRaw();
        instance.nextQuestion++;
        if (instance.nextQuestion >= this.questions.size()) {
            bugReportInstances.remove(event.getAuthor().getIdLong());
            event.getChannel().sendMessage("Thank you for your bug report!").queue();
            this.gitHubBot.createBugIssue(instance.responses[0], instance.responses[1], instance.responses[2],
                    instance.responses[3], event.getAuthor().getName() + "#" +
                            event.getAuthor().getDiscriminator(), event.getAuthor().getId());
        } else {
            event.getChannel().sendMessage(this.questions.get(instance.nextQuestion)).queue();
            instance.setIdleInstanceCleanup();
        }
    }
}
