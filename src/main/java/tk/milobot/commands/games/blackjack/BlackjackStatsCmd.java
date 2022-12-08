package tk.milobot.commands.games.blackjack;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import tk.milobot.database.dao.BlackjackDao;
import tk.milobot.database.model.Blackjack;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class BlackjackStatsCmd extends Command implements SubCmd {

    private final BlackjackDao blackjackDao = BlackjackDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(BlackjackStatsCmd.class);

    public BlackjackStatsCmd() {
        this.commandName = "stats";
        this.commandDescription = "View your own blackjack statistics.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        try {
            EmbedBuilder embedBuilder = generateEmbed(event.getAuthor());
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
                    Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
        } catch (SQLException e) {
            logger.error("SQL Error while generating embed for user " + event.getAuthor().getId(), e);
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        try {
            EmbedBuilder embedBuilder = generateEmbed(event.getUser());
            event.replyEmbeds(embedBuilder.build()).addActionRow(
                    Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
        } catch (SQLException e) {
            logger.error("SQL Error while generating embed for user " + event.getUser().getId(), e);
        }
    }

    private @NotNull EmbedBuilder generateEmbed(User user) throws SQLException {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, user);
        embed.setTitle(String.format("Blackjack Statistics for %s", user.getName()));

        Blackjack blackjack;
        try (Connection con = DatabaseConnection.getConnection()) {
            blackjack = blackjackDao.getByUserDiscordId(con, user.getIdLong(), RowLockType.NONE);
        }
        if (blackjack != null) {
            int currentStreak = blackjack.getStreak();
            int highestStreak = blackjack.getHighestStreak();
            int totalGames = blackjack.getTotalGames();
            int totalWins = blackjack.getTotalWins();
            int totalLosses = blackjack.getTotalGames() - blackjack.getTotalWins() - blackjack.getTotalDraws();
            int totalDraws = blackjack.getTotalDraws();
            int totalEarnings = blackjack.getTotalEarnings();

            embed.addField("Total Games", String.valueOf(totalGames), true);
            embed.addField("Total Wins", String.valueOf(totalWins), true);
            embed.addField("Total Draws", String.valueOf(totalDraws), true);
            embed.addField("Total Losses", String.valueOf(totalLosses), true);
            embed.addField("Current Streak", String.valueOf(currentStreak), true);
            embed.addField("Highest Streak", String.valueOf(highestStreak), true);
            embed.addField("Total Earnings", totalEarnings + " morbcoins", true);
        } else {
            embed.setDescription("No blackjack statistics on record.");
        }

        return embed;
    }

}
