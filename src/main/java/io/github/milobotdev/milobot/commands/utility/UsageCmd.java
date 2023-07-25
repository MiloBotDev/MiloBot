package io.github.milobotdev.milobot.commands.utility;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.ParentSlashCommandData;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.database.dao.CommandTrackerDao;
import io.github.milobotdev.milobot.database.model.CommandTracker;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

public class UsageCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs, UtilityCmd {

    private static final Logger logger = LoggerFactory.getLogger(UsageCmd.class);
    private final ExecutorService executorService;
    private final CommandTrackerDao commandTrackerDao;

    public UsageCmd(ExecutorService executorService) {
        this.executorService = executorService;
        this.commandTrackerDao = CommandTrackerDao.getInstance();
    }

    @Override
    public @NotNull ParentSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSlashCommandData(
                Commands.slash("usage", "Shows command usage statistics.")
        );
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        try {
            EmbedBuilder embed = generateUsageEmbed(event.getUser());
            event.replyEmbeds(embed.build()).queue();
        } catch (SQLException e) {
            logger.error("Error generating usage embed", e);
            event.reply("Something went wrong, please try again later.").setEphemeral(true).queue();
        }
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        try {
            EmbedBuilder embed = generateUsageEmbed(event.getAuthor());
            event.getChannel().sendMessageEmbeds(embed.build()).queue();
        } catch (SQLException e) {
            logger.error("Error generating usage embed", e);
            event.getChannel().sendMessage("Something went wrong, please try again later.").queue();
        }
    }

    private @NotNull EmbedBuilder generateUsageEmbed(User user) throws SQLException {
        EmbedBuilder usageEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(usageEmbed, user);
        usageEmbed.setTitle("Command Usage Statistics");

        List<CommandTracker> totalCommandUsage = commandTrackerDao.getTotalCommandUsage();

        AtomicInteger totalUsage = new AtomicInteger();
        totalCommandUsage.forEach(commandTracker -> totalUsage.addAndGet(commandTracker.getCount()));
        usageEmbed.addField("Total Command Usage", String.valueOf(totalCommandUsage.size()), true);
        usageEmbed.addField("Most Used Command", String.valueOf(totalCommandUsage.get(0).getCommand()), true);
        usageEmbed.addField("Least Used Command", totalCommandUsage.get(totalCommandUsage.size() - 1).getCommand(), true);


        return usageEmbed;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
