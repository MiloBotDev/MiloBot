package commands.games.blackjack;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.BlackjackTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;

public class BlackjackStatsCmd extends Command implements SubCmd {

    private final DatabaseManager dbManager;

    public BlackjackStatsCmd() {
        this.commandName = "stats";
        this.commandDescription = "View your own blackjack statistics.";
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        EmbedBuilder embedBuilder = generateEmbed(event.getAuthor());
        event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        EmbedBuilder embedBuilder = generateEmbed(event.getUser());
        event.replyEmbeds(embedBuilder.build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }

    private @NotNull EmbedBuilder generateEmbed(User user) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle(String.format("Blackjack Statistics for %s", user.getName()));

        ArrayList<String> result = dbManager.query(BlackjackTableQueries.getUser, DatabaseManager.QueryTypes.RETURN, user.getId());
        if(result.size() != 0) {
            String currentStreak = result.get(2);
            String totalGames = result.get(3);
            String totalWins = result.get(4);
            String totalEarnings = result.get(5);
            String totalDraws = result.get(6);
            String highestStreak = result.get(7);
            String totalLosses = String.valueOf(Integer.parseInt(totalGames) - Integer.parseInt(totalWins) - Integer.parseInt(totalDraws));

            embed.addField("Total Games", totalGames, true);
            embed.addField("Total Wins", totalWins, true);
            embed.addField("Total Draws", totalDraws, true);
            embed.addField("Total Losses", totalLosses, true);
            embed.addField("Current Streak", currentStreak, true);
            embed.addField("Highest Streak", highestStreak, true);
            embed.addField("Total Earnings", totalEarnings + " morbcoins", true);
        } else {
            embed.setDescription("No blackjack statistics on record.");
        }

        return embed;
    }

}
