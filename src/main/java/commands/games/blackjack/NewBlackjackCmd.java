package commands.games.blackjack;

import commands.newcommand.ParentCommand;
import commands.newcommand.extensions.Aliases;
import commands.newcommand.extensions.DefaultFlags;
import commands.newcommand.extensions.DefaultSlashParentCommand;
import commands.newcommand.extensions.DefaultTextParentCommand;
import net.dv8tion.jda.api.entities.ChannelType;
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
        /*this.commandName = "blackjack";
        this.commandDescription = "Blackjack brought to discord.";
        this.aliases = new String[]{"bj"};
        this.subCommands.add(new BlackjackPlayCmd());
        this.subCommands.add(new BlackjackStatsCmd());
        this.subCommands.add(new BlackjackInfoCmd());
        this.subCommands.add(new BlackjackLeaderboardCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);*/
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("blackjack", "Blackjack brought to discord.");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.TEXT, ChannelType.PRIVATE);
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("bj");
    }
}
