package io.github.milobotdev.milobot.commands.bot.bug;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.utility.Config;
import io.github.milobotdev.milobot.utility.GitHubBot;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class BugReportCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags, DefaultCommandArgs, DefaultChannelTypes, EventListeners {

    private final ExecutorService executorService;
    private final List<String> questions = List.of(new String[]{
        "Please give me a short summary of the bug you found. You can type cancel at any time to stop the command.",
                "How do you reproduce the bug?",
                "On a scale of 1-5, how severe would you say the bug is?",
                "Do you have any additional information about this bug?",
    });
    private final GitHubBot gitHubBot = GitHubBot.getInstance();
    private final ScheduledExecutorService idleInstanceCleanupExecutorService = Executors.newScheduledThreadPool(1);

    @Override
    public @NotNull List<EventListener> getEventListeners() {
        return List.of(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                onMessage(event);
            }
        });
    }

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

    public BugReportCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("report", "Report a bug you found.");
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
    public void executeCommand(SlashCommandInteractionEvent event) {
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

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
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
