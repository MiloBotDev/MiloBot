package tk.milobot.commands.bot.bug;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import tk.milobot.commands.newcommand.ParentCommand;
import tk.milobot.commands.newcommand.extensions.*;

import java.util.Set;
import java.util.concurrent.ExecutorService;

public class NewBugCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultChannelTypes,
        DefaultTextParentCommand, DefaultSlashParentCommand, DefaultFlags {

    private final ExecutorService executorService;

    public NewBugCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
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
