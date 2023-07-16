package io.github.milobotdev.milobot.commands.games.blackjack;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.games.BlackjackGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BlackjackPlayCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, Instance {

    private final ExecutorService executorService;
    // TODO : Figure out a better way to request the instance data
    public static InstanceData instanceData;

    public BlackjackPlayCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        BlackjackGame.newGame(event, args, isInstanced().duration());
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        BlackjackGame.newGame(event, isInstanced().duration());
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("play", "Play a game of blackjack on discord.")
                .addOptions(new OptionData(OptionType.INTEGER, "bet", "The amount of money you want to bet.", false)
                        .setRequiredRange(1, 10000));
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull List<String> getCommandArgs() {
        return List.of("bet*");
    }

    @Override
    public boolean checkRequiredArgs(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (args.size() < 1) {
            return true;
        } else if (args.size() > 1) {
            sendTooManyArgs(event);
            return false;
        } else {
            try {
                int bet = Integer.parseInt(args.get(0));
                if (bet < 1 || bet > 10000) {
                    sendInvalidArgs(event, "The bet must be between 1 and 10000.");
                    return false;
                }
            } catch (NumberFormatException e) {
                sendInvalidArgs(event, "The bet must be a number.");
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public InstanceData isInstanced() {
        instanceData = new InstanceData(true, 900, new GameType("blackjack", userId -> {
            BlackjackGame blackjackGame = BlackjackGame.blackjackGames.get(userId);
            blackjackGame.getMessage().delete().queue();
            BlackjackGame.blackjackGames.remove(userId);
        }, false, null));
        return instanceData;
    }
}
