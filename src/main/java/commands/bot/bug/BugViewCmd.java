package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;
import utility.EmbedUtils;
import utility.GitHubBot;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Lookup a specific bug on the issue tracker.
 */
public class BugViewCmd extends Command implements SubCmd {

    private final GitHubBot gitHubBot;

    public BugViewCmd() {
        this.commandName = "view";
        this.commandDescription = "Lookup a specific bug on the issue tracker.";
        this.commandArgs = new String[]{"id"};

        this.gitHubBot = GitHubBot.getInstance();
    }


    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (args.size() < 1) {
            sendCommandUsage(event);
        } else {
            Optional<EmbedBuilder> loadedBug = loadBug(Integer.parseInt(args.get(0)), event.getAuthor());
            if (loadedBug.isEmpty()) {
                event.getChannel().sendMessage(String.format("Bug with number: %s not found.", args.get(0))).queue();
            } else {
                event.getChannel().sendMessageEmbeds(loadedBug.get().build()).setActionRow(
                        Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
            }
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        int id = Math.toIntExact(Objects.requireNonNull(event.getOption("id")).getAsLong());
        Optional<EmbedBuilder> loadedBug = loadBug(id, event.getUser());
        if (loadedBug.isEmpty()) {
            event.reply(String.format("Bug with number: %d not found.", id)).queue();
        } else {
            event.replyEmbeds(loadedBug.get().build()).addActionRow(
                    Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
        }

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
