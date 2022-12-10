package tk.milobot.commands.games.blackjack;

import net.dv8tion.jda.api.entities.ChannelType;
import tk.milobot.commands.newcommand.ParentCommand;
import tk.milobot.commands.newcommand.extensions.*;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class NewBlackjackCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand,
        Aliases, DefaultFlags {
    private final ExecutorService executorService;

    public NewBlackjackCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("blackjack", "Blackjack brought to discord.");
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("bj");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.PRIVATE, ChannelType.TEXT);
    }
}
