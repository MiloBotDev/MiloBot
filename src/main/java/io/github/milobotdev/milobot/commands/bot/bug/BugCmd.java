package io.github.milobotdev.milobot.commands.bot.bug;

import io.github.milobotdev.milobot.commands.bot.BotCmd;
import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BugCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultChannelTypes,
        DefaultTextParentCommand, DefaultSlashParentCommand, DefaultFlags, BotCmd {

    private final ExecutorService executorService;

    public BugCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new CommandData("bug", "Add bugs to the bots issue tracker, or view them.");
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
