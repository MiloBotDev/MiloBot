package io.github.milobotdev.milobot.commands.bot.bug;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.GitHubBot;
import io.github.milobotdev.milobot.utility.paginator.Paginator;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.kohsuke.github.GHIssue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BugListCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs, DefaultFlags, DefaultChannelTypes {

    private final ExecutorService executorService;
    private final GitHubBot gitHubBot = GitHubBot.getInstance();


    public BugListCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(new SubcommandData("list", "Shows a list of all reported bugs."));
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        ArrayList<MessageEmbed> pages = createPages(event.getAuthor());
        if (pages.size() == 0) {
            event.getChannel().sendMessage("There are no reported bugs.").queue();
        } else {
            Paginator paginator = new Paginator(event.getAuthor(), pages);
            event.getChannel().sendMessageEmbeds(paginator.currentPage())
                    .setComponents(paginator.getActionRows())
                    .queue(paginator::initialize);
        }
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        ArrayList<MessageEmbed> pages = createPages(event.getUser());
        if (pages.size() == 0) {
            event.getChannel().sendMessage("There are no reported bugs.").queue();
        } else {
            Paginator paginator = new Paginator(event.getUser(), pages);
            event.getHook().sendMessageEmbeds(paginator.currentPage())
                    .addComponents(paginator.getActionRows())
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

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }
}
