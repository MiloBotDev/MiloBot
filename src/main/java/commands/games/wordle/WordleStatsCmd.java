package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.WordleTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WordleStatsCmd extends Command implements SubCmd {

    private final DatabaseManager dbManager;

    public WordleStatsCmd() {
        this.commandName = "stats";
        this.commandDescription = "View your own wordle statistics";
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
        embed.setTitle(String.format("Wordle Statistics for %s", user.getName()));

        ArrayList<String> result = dbManager.query(WordleTableQueries.selectUserWordle, DatabaseManager.QueryTypes.RETURN, user.getId());
        if(result.size() == 0) {
            embed.setDescription("No wordle statistics on record.");
        } else  {
            String fastestTime = result.get(1);
            String currentStreak = result.get(3);
            String totalGames = result.get(4);
            String highestStreak = result.get(5);

            if(Objects.equals(fastestTime, "null")) {
                fastestTime = "0";
            }

            embed.addField("Total Games", totalGames, true);
            embed.addField("Current Streak", currentStreak, true);
            embed.addField("Highest Streak", highestStreak, true);
            embed.addField("Fastest Time", fastestTime + " Seconds", true);
        }

        return embed;
    }
}
