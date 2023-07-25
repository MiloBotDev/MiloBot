package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SlashCommandDataUtils;
import io.github.milobotdev.milobot.commands.command.extensions.slashcommands.SubSlashCommandData;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.games.WordleGame;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Play a game of wordle.
 */
public class WordlePlayCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes, Instance {

    private final ExecutorService executorService;

    public WordlePlayCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull SubSlashCommandData getCommandData() {
        return SlashCommandDataUtils.fromSubCommandData(new SubcommandData("play", "Play a game of wordle."));
    }


    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        WordleGame wordleGame = new WordleGame(event.getAuthor().getIdLong());
        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        wordleEmbed.setDescription("Every 5 letter word you type will be inputted as a guess.");
        EmbedUtils.styleEmbed(wordleEmbed, event.getAuthor());
        event.getChannel().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            wordleGame.attachMessage(message, wordleEmbed);
        });
    }

    @Override
    public void executeCommand(@NotNull SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        WordleGame wordleGame = new WordleGame(event.getUser().getIdLong());
        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        wordleEmbed.setDescription("Every 5 letter word you type will be inputted as a guess.");
        EmbedUtils.styleEmbed(wordleEmbed, event.getUser());
        event.getHook().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            wordleGame.attachMessage(message, wordleEmbed);
        });
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public InstanceData isInstanced() {
        return new InstanceData(true, 900, new GameType("wordle",
                userId -> WordleGame.wordleGames.get(userId).removeGame(userId), false, null));
    }
}
