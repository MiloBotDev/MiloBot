package io.github.milobotdev.milobot.commands.games.hungergames;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.database.dao.HungerGamesDao;
import io.github.milobotdev.milobot.database.model.HungerGames;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class HungerGamesStatsCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs {

    private final ExecutorService executorService;
    private final HungerGamesDao hungerGamesDao = HungerGamesDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(HungerGamesStatsCmd.class);

    public HungerGamesStatsCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("stats", "View your own hungergames statistics.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        try {
            EmbedBuilder embedBuilder = generateEmbed(event.getAuthor());
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
                    Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
        } catch (SQLException e) {
            logger.error("SQL Error while generating embed for user " + event.getAuthor().getId(), e);
        }
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
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

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }
}
