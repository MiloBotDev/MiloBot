package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import newdb.dao.WordleDao;
import newdb.model.Wordle;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * View all the leaderboards for the Wordle command.
 */
public class WordleLeaderboardCmd extends Command implements SubCmd {

    private final WordleDao wordleDao;

    public WordleLeaderboardCmd() {
        this.commandName = "leaderboard";
        this.commandDescription = "View the wordle leaderboards.";
        this.commandArgs = new String[]{"*leaderboard"};
        this.wordleDao = WordleDao.getInstance();
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        try {
            List<Wordle> topHighestStreak = this.wordleDao.getTopHighestStreak();
            ArrayList<EmbedBuilder> highest_streak = buildEmbeds(topHighestStreak, "Highest Streak", event.getJDA());
            event.getChannel().sendMessageEmbeds(highest_streak.get(0).build()).queue();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        event.getJDA();
    }

    private @NotNull ArrayList<EmbedBuilder> buildEmbeds(@NotNull List<Wordle> wordles, String title, JDA jda) {
        ArrayList<EmbedBuilder> embeds = new ArrayList<>();

        final EmbedBuilder[] embed = {new EmbedBuilder()};
        final StringBuilder[] desc = {new StringBuilder()};
        embed[0].setTitle(title);

        final int[] counter = {1};
        wordles.forEach((wordle) -> {
            // TODO: use names instead of ids
            desc[0].append(String.format("`%d.` %d - %d games", counter[0], wordle.getUserId(), wordle.getHighestStreak()));
            counter[0]++;
            if(counter[0] % 10 == 0) {
                embed[0].setDescription(desc[0]);
                embeds.add(embed[0]);
                embed[0] = new EmbedBuilder();
                desc[0] = new StringBuilder();
                embed[0].setTitle(title);
            }
        });

        embed[0].setDescription(desc[0]);
        embeds.add(embed[0]);

        return embeds;
    }

}

