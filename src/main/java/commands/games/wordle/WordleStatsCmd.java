package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import database.dao.UserDao;
import database.dao.WordleDao;
import database.model.Wordle;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class WordleStatsCmd extends Command implements SubCmd {

    private final WordleDao wordleDao;
    private final UserDao userDao;

    public WordleStatsCmd() {
        this.commandName = "stats";
        this.commandDescription = "View your own wordle statistics";
        this.wordleDao = WordleDao.getInstance();
        this.userDao = UserDao.getInstance();
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
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
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            EmbedBuilder embed = new EmbedBuilder();
            EmbedUtils.styleEmbed(embed, user);
            embed.setTitle(String.format("Wordle Statistics for %s", user.getName()));

            int id = Objects.requireNonNull(userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE)).getId();
            Wordle userWordle = wordleDao.getByUserId(con, id, RowLockType.NONE);
            con.commit();
            if (userWordle == null) {
                embed.setDescription("No wordle statistics on record.");
            } else {
                int fastestTime = userWordle.getFastestTime();
                int currentStreak = userWordle.getCurrentStreak();
                int totalGames = userWordle.getGamesPlayed();
                int highestStreak = userWordle.getHighestStreak();
                int totalWins = userWordle.getWins();
                int totalLosses = totalGames - totalWins;

                embed.addField("Total Games", String.valueOf(totalGames), true);
                embed.addField("Total Wins", String.valueOf(totalWins), true);
                embed.addField("Total Losses", String.valueOf(totalLosses), true);
                embed.addField("Current Streak", String.valueOf(currentStreak), true);
                embed.addField("Highest Streak", String.valueOf(highestStreak), true);

                if (fastestTime != 0) {
                    embed.addField("Fastest Time", fastestTime + " Seconds", true);
                } else {
                    embed.addField("Fastest Time", "None", true);
                }
            }
            return embed;
        } catch (SQLException e) {
            return new EmbedBuilder().setTitle("Error").setDescription("An error occurred while fetching your wordle statistics.");
        }
    }
}
