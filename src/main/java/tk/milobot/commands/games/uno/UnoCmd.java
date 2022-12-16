package tk.milobot.commands.games.uno;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;
import tk.milobot.commands.command.ParentCommand;
import tk.milobot.commands.command.extensions.*;
import tk.milobot.commands.games.GamesCmd;
import tk.milobot.games.uno.UnoGame;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class UnoCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand,
        DefaultFlags, DefaultChannelTypes, EventListeners, GamesCmd {

    private final ExecutorService executorService;

    public UnoCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("uno", "Play Uno with your friends!");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    private final ListenerAdapter unoListener = new ListenerAdapter() {
        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            UnoGame.onMessageReceived(event);
        }
    };

    @Override
    public @NotNull List<EventListener> getEventListeners() {
        return List.of(unoListener);
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
