package io.github.milobotdev.milobot.commands.bot.bug;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.CommonSlashCommandData;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.GitHubBot;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BugViewCmd extends SubCommand implements TextCommand, SlashCommand, DefaultChannelTypes, DefaultFlags {

    private final ExecutorService executorService;
    private final GitHubBot gitHubBot = GitHubBot.getInstance();

    public BugViewCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        Optional<EmbedBuilder> loadedBug = loadBug(Integer.parseInt(args.get(0)), event.getAuthor());
        if (loadedBug.isEmpty()) {
            event.getChannel().sendMessage(String.format("Bug with number: %s not found.", args.get(0))).queue();
        } else {
            event.getChannel().sendMessageEmbeds(loadedBug.get().build()).setActionRow(
                    Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
        }
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("id");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if (args.size() !=  1) {
            sendMissingArgs(event);
            return false;
        } else {
            try {
                Integer.parseInt(args.get(0));
                return true;
            } catch (NumberFormatException e) {
                sendInvalidArgs(event, "Bug id must be a number.");
                return false;
            }
        }
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        int id = Math.toIntExact(Objects.requireNonNull(event.getOption("id")).getAsLong());
        Optional<EmbedBuilder> loadedBug = loadBug(id, event.getUser());
        if (loadedBug.isEmpty()) {
            event.reply(String.format("Bug with number: %d not found.", id)).queue();
        } else {
            event.replyEmbeds(loadedBug.get().build()).addActionRow(
                    Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
        }
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(
                new SubcommandData("view", "Lookup a specific bug on the issue tracker.")
                        .addOptions(
                                new OptionData(OptionType.INTEGER, "id", "The id of the bug you want to view", true)
                        )
        );
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    /**
     * Tries to load the bug with the given id and formats it in an embed.
     */
    private Optional<EmbedBuilder> loadBug(int id, User user) {
        Optional<GHIssue> bug = gitHubBot.getBug(id);
        if (bug.isEmpty()) {
            return Optional.empty();
        } else {
            GHIssue ghIssue = bug.get();
            EmbedBuilder embed = new EmbedBuilder();
            EmbedUtils.styleEmbed(embed, user);
            embed.setTitle(ghIssue.getTitle());
            String body = ghIssue.getBody().replaceAll("#", "")
                    .replaceAll("Steps to Reproduce", "**Steps to Reproduce:**")
                    .replaceAll("Severity", "**Severity:**")
                    .replaceAll("Additional Information", "**Additional Information:**")
                    .replaceAll("Author", "**Author:**");
            embed.setDescription(body);
            return Optional.of(embed);
        }

    }
}
