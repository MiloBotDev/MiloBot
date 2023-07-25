package io.github.milobotdev.milobot.commands.games.poker;

import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.ParentSlashCommandData;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.games.GamesCmd;
import io.github.milobotdev.milobot.games.PokerGame;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class PokerCmd extends ParentCommand implements DefaultTextParentCommand, DefaultSlashParentCommand,
        DefaultFlags, DefaultChannelTypes, EventListeners, GamesCmd {

    private final ExecutorService executorService;

    public PokerCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull ParentSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSlashCommandData(Commands.slash("poker", "5-card Poker brought to discord."));
    }

    private final ListenerAdapter pokerListener = new ListenerAdapter() {
        @Override
        public void onMessageReceived(@NotNull MessageReceivedEvent event) {
            PokerGame.onMessageReceived(event);
        }
    };

    @Override
    public @NotNull List<EventListener> getEventListeners() {
        return List.of(pokerListener);
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
