package tk.milobot.commands.bot.bug;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;
import tk.milobot.utility.EmbedUtils;
import tk.milobot.utility.GitHubBot;
import tk.milobot.utility.Paginator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Displays all issues labeled as a bug.
 */
public class BugListCmd extends Command implements SubCmd {

    private final static ResourceBundle resourceBundle = ResourceBundle.getBundle("localization.MiloBot_en_US", Locale.getDefault());
    private final GitHubBot gitHubBot;

    public BugListCmd() {
        this.commandName = resourceBundle.getString("bugListCommandName");
        this.commandDescription = resourceBundle.getString("bugListCommandDescription");
        this.gitHubBot = GitHubBot.getInstance();
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        ArrayList<MessageEmbed> pages = createPages(event.getAuthor());
        if (pages.size() == 0) {
            event.getChannel().sendMessage("There are no reported bugs.").queue();
        } else {
            Paginator paginator = new Paginator(event.getAuthor(), pages);
            event.getChannel().sendMessageEmbeds(paginator.currentPage()).setActionRows(paginator.getActionRows())
                    .queue(paginator::initialize);
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        ArrayList<MessageEmbed> pages = createPages(event.getUser());
        if (pages.size() == 0) {
            event.getChannel().sendMessage("There are no reported bugs.").queue();
        } else {
            Paginator paginator = new Paginator(event.getUser(), pages);
            event.getHook().sendMessageEmbeds(paginator.currentPage()).addActionRows(paginator.getActionRows())
                    .queue(paginator::initialize);
        }
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
            String title = ghIssue.getTitle();
            if (title.length() > 50) {
                title = title.substring(0, 50) + "...";
            }
            description.append(String.format("`%s:` %s\n", ghIssue.getNumber(), title));
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
