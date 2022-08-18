package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.WordleTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;
import utility.Paginator;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * View all the leaderboards for the Wordle command.
 */
public class WordleLeaderboardCmd extends Command implements SubCmd {

    private static final DatabaseManager manager = DatabaseManager.getInstance();

    public WordleLeaderboardCmd() {
        this.commandName = "leaderboard";
        this.commandDescription = "View the wordle leaderboards.";
        this.commandArgs = new String[]{"*leaderboard"};
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        User author = event.getAuthor();
        String authorId = author.getId();

        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, author);
        embed.setTitle("Leaderboards");
        String description = "The following leaderboards are available:\n" +
                "- **Total games played.**\n" +
                "- **Highest streak.** \n" +
                "- **Current streak.** \n";
        embed.setDescription(description);

        if (args.size() == 0) {
            event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                    Button.secondary(authorId + ":delete", "Delete")
            ).queue();
        } else {
            String leaderboard = String.join("", args).toLowerCase(Locale.ROOT);
            ArrayList<EmbedBuilder> embeds = null;
            if (leaderboard.contains("total")) {
                embeds = makeLeaderboardEmbeds(event.getAuthor(), "Top Total Games Played",
                        WordleTableQueries.wordleGetTopTotalGamesPlayed);
            } else if (leaderboard.contains("highest")) {
                embeds = makeLeaderboardEmbeds(event.getAuthor(), "Top Highest Streak",
                        WordleTableQueries.wordleGetTopHighestStreak);
            } else if (leaderboard.contains("current")) {
                embeds = makeLeaderboardEmbeds(event.getAuthor(), "Top Current Streak",
                        WordleTableQueries.wordleGetTopCurrentStreak);
            }
            if (embeds == null) {
                event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                        Button.secondary(authorId + ":delete", "Delete")
                ).queue();
            } else {
                Paginator pager = new Paginator(embeds.get(0));
                embeds.remove(0);
                pager.addPages(embeds);
                event.getChannel().sendMessageEmbeds(pager.currentPage().build()).setActionRows(ActionRow.of(
                        Button.primary(authorId + ":previousPage", "Previous"),
                        Button.secondary(authorId + ":delete", "Delete"),
                        Button.primary(authorId + ":nextPage", "Next")
                )).queue(message -> pager.initialize(message.getId()));
            }
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        User user = event.getUser();
        String totalGamesPlayed = event.getOption("leaderboard").getAsString();
        ArrayList<EmbedBuilder> embeds = null;
        switch (totalGamesPlayed) {
            case "totalGamesPlayed":
                embeds = makeLeaderboardEmbeds(user, "Top Total Games Played",
                        WordleTableQueries.wordleGetTopTotalGamesPlayed);
                break;
            case "highestStreak":
                embeds = makeLeaderboardEmbeds(user, "Top Highest Streak",
                        WordleTableQueries.wordleGetTopHighestStreak);
                break;
            case "currentStreak":
                embeds = makeLeaderboardEmbeds(user, "Top Current Streak",
                        WordleTableQueries.wordleGetTopCurrentStreak);
                break;
        }
        if (embeds == null) {
            // this should never happen
            return;
        }
        Paginator pager = new Paginator(embeds.get(0));
        embeds.remove(0);
        pager.addPages(embeds);
        String id = user.getId();
        event.getHook().sendMessageEmbeds(pager.currentPage().build()).addActionRows(ActionRow.of(
                Button.primary(id + ":previousPage", "Previous"),
                Button.secondary(id + ":delete", "Delete"),
                Button.primary(id + ":nextPage", "Next")
        )).queue(message -> pager.initialize(message.getId()));
    }


    private @NotNull ArrayList<EmbedBuilder> makeLeaderboardEmbeds(User author, String title, String query) {
        ArrayList<EmbedBuilder> embedPages = new ArrayList<>();
        ArrayList<String> result = manager.query(query, DatabaseManager.QueryTypes.RETURN);

        int rowCount = 0;
        int rank = 1;
        int currentPage = 1;
        int totalPages = 1;

        // calculate the amount of pages that will be generated
        for (int i = 0; i < result.size(); i += 2) {
            rowCount++;
            if (rowCount == 15) {
                rowCount = 0;
                totalPages++;
            }

        }

        EmbedBuilder page = new EmbedBuilder();
        EmbedUtils.styleEmbed(page, author);
        page.setTitle(title);
        page.setFooter(String.format("Page %d/%d", currentPage, totalPages));

        rowCount = 0;
        StringBuilder description = new StringBuilder();
        for (int i = 0; i < result.size(); i += 2) {
            description.append(String.format("`%d`: %s - %s total games.\n", rank, result.get(i), result.get(i + 1)));
            if (i + 2 == result.size()) {
                page.setDescription(description.toString());
                embedPages.add(page);
                break;
            }
            rowCount++;
            if (rowCount == 15) {
                currentPage++;
                page.setDescription(description.toString());
                embedPages.add(page);

                rowCount = 0;
                page = new EmbedBuilder();
                page.setFooter(String.format("Page %d/%d", currentPage, totalPages));
                EmbedUtils.styleEmbed(page, author);
                page.setTitle(title);
                description = new StringBuilder();
            }
            rank++;
        }

        return embedPages;
    }

}

