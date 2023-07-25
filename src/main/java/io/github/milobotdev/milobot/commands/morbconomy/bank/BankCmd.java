package io.github.milobotdev.milobot.commands.morbconomy.bank;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultSlashParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultTextParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.ParentSlashCommandData;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BankCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand,
        DefaultFlags, DefaultChannelTypes, MorbconomyCmd {

    private final ExecutorService executorService;

    public BankCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull ParentSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSlashCommandData(
                Commands.slash("bank", "All commands related to your virtual bank.")
        );
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
