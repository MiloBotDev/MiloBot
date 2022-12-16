package tk.milobot.commands.morbconomy.bank;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import tk.milobot.commands.command.ParentCommand;
import tk.milobot.commands.command.extensions.DefaultChannelTypes;
import tk.milobot.commands.command.extensions.DefaultFlags;
import tk.milobot.commands.command.extensions.DefaultSlashParentCommand;
import tk.milobot.commands.command.extensions.DefaultTextParentCommand;
import tk.milobot.commands.morbconomy.MorbconomyCmd;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BankCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand,
        DefaultFlags, DefaultChannelTypes, MorbconomyCmd {

    private final ExecutorService executorService;

    public BankCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("bank", "All commands related to your virtual bank.");
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
