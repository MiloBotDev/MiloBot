package commands.bot.bug;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
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
        ArrayList<MessageEmbed> pages = createPages(event.getAuthor());
        MessageEmbed startingEmbed = pages.get(0);
        Paginator paginator = new Paginator();
        paginator.addPages(pages);
        String id = event.getAuthor().getId();
        event.getChannel().sendMessageEmbeds(startingEmbed).setActionRows(ActionRow.of(
                Button.primary(id + ":previousPage", "Previous"),
                Button.secondary(id + ":deletePaginator", "Delete"),
                Button.primary(id + ":nextPage", "Next")
        )).queue(paginator::initialize);
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        ArrayList<MessageEmbed> pages = createPages(event.getUser());
        MessageEmbed startingEmbed = pages.get(0);
        Paginator paginator = new Paginator();
        paginator.addPages(pages);
        String id = event.getUser().getId();
        event.getHook().sendMessageEmbeds(startingEmbed).addActionRows(ActionRow.of(
                Button.primary(id + ":previousPage", "Previous"),
                Button.secondary(id + ":deletePaginator", "Delete"),
                Button.primary(id + ":nextPage", "Next")
        )).queue(paginator::initialize);
    }

    /**
     * Creates all pages for the paginator.
     */
    private @NotNull ArrayList<MessageEmbed> createPages(User user) {
        ArrayList<MessageEmbed> pages = new ArrayList<>();
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
                pages.add(page.build());
                break;
            }
            rowCount++;
            if (rowCount == 10) {
                rowCount = 0;
                page.setDescription(description.toString());
                pages.add(page.build());
                page = new EmbedBuilder();
                EmbedUtils.styleEmbed(page, user);
                page.setTitle("Bugs");
                description = new StringBuilder();
            }
        }
        return pages;
    }
}
