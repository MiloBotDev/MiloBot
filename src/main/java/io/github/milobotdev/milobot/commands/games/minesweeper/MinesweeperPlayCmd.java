package io.github.milobotdev.milobot.commands.games.minesweeper;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.games.Minesweeper;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class MinesweeperPlayCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, Aliases, DefaultCommandArgs, Instance {

    private static final Logger logger = LoggerFactory.getLogger(MinesweeperPlayCmd.class);
    private final ExecutorService executorService;

    public MinesweeperPlayCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("play", "Play a game of minesweeper");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        event.getMessage().reply("A game has been started in your dms.").queue();
        User author = event.getAuthor();
        author.openPrivateChannel().queue(privateChannel -> {
                try {
                    Minesweeper minesweeper = new Minesweeper(author);
                    EmbedBuilder embedBuilder = new EmbedBuilder();
                    embedBuilder.setTitle("Minesweeper");
                    embedBuilder.setImage("attachment://minesweeper.png");
                    privateChannel.sendMessageEmbeds(embedBuilder.build()).addFile(minesweeper.boardToPng(), "minesweeper.png").queue();
                } catch (IOException | URISyntaxException e) {
                    logger.error("Error starting minesweeper game ", e);
                }
            });
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        event.reply("A game has been started in your dms.").queue();
        User user = event.getUser();
        user.openPrivateChannel().queue(privateChannel -> {
            try {
                Minesweeper minesweeper = new Minesweeper(user);
                EmbedBuilder embedBuilder = new EmbedBuilder();
                embedBuilder.setTitle("Minesweeper");
                embedBuilder.setImage("attachment://minesweeper.png");
                privateChannel.sendMessageEmbeds(embedBuilder.build()).addFile(minesweeper.boardToPng(), "minesweeper.png").queue();
            } catch (IOException | URISyntaxException e) {
                logger.error("Error starting minesweeper game ", e);
            }
        });
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull List<String> getAliases() {
        return List.of("p");
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public InstanceData isInstanced() {
        return new InstanceData(true, 1200,
                new GameType("minesweeper", userId -> Minesweeper.minesweeperGames.get(userId).removeGame(userId),
                        false, null));
    }
}
