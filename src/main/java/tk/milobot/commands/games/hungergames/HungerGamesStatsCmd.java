package tk.milobot.commands.games.hungergames;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.database.dao.HungerGamesDao;
import tk.milobot.database.model.HungerGames;
import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class HungerGamesStatsCmd extends Command implements SubCmd {

    private final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(HungerGamesStatsCmd.class);

    public HungerGamesStatsCmd() {
        this.commandName = "stats";
        this.commandDescription = "View your own hungergames statistics.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
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
        embed.setTitle("HungerGames Statistics for " + user.getName());

        try(Connection con = DatabaseConnection.getConnection()) {
            HungerGames hungerGames = hungerGamesDao.getByUserDiscordId(con, user.getIdLong(), RowLockType.NONE);
            if(hungerGames != null) {
                int totalKills = hungerGames.getTotalKills();
                int totalDamageDone = hungerGames.getTotalDamageDone();
                int totalDamageTaken = hungerGames.getTotalDamageTaken();
                int totalHealingDone = hungerGames.getTotalHealingDone();
                int totalItemsCollected = hungerGames.getTotalItemsCollected();
                int totalGamesPlayed = hungerGames.getTotalGamesPlayed();
                int totalWins = hungerGames.getTotalWins();

                embed.addField("Total Kills", String.valueOf(totalKills), true);
                embed.addField("Total Damage Done", String.valueOf(totalDamageDone), true);
                embed.addField("Total Damage Taken", String.valueOf(totalDamageTaken), true);
                embed.addField("Total Healing Done", String.valueOf(totalHealingDone), true);
                embed.addField("Total Items Collected", String.valueOf(totalItemsCollected), true);
                embed.addField("Total Games Played", String.valueOf(totalGamesPlayed), true);
                embed.addField("Total Wins", String.valueOf(totalWins), true);
            } else {
                embed.setDescription("No hungergames statistics on record.");
            }
        }
        return embed;
    }
}