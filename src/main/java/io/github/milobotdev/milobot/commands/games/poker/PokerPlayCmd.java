package io.github.milobotdev.milobot.commands.games.poker;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.commands.instance.model.RemoveInstance;
import io.github.milobotdev.milobot.games.PokerGame;
import io.github.milobotdev.milobot.utility.lobby.Lobby;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class PokerPlayCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags, Aliases,
        DefaultCommandArgs {

    private final ExecutorService executorService;

    public PokerPlayCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("play", "Play a game of poker on discord.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        new Lobby("Poker lobby", event.getAuthor(),
                (players, message) -> {
                    PokerGame pokerGame = new PokerGame(players);
                    pokerGame.start();
                }, 2, 5).initialize(event.getChannel());
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        new Lobby("Poker lobby", event.getUser(),
                (players, message) -> {
                    PokerGame pokerGame = new PokerGame(players);
                    pokerGame.start();
                }, 2, 5).initialize(event);
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("start", "host");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.TEXT);
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

}
