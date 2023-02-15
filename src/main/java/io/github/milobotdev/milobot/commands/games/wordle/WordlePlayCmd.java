package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.WordleDao;
import io.github.milobotdev.milobot.database.model.Wordle;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.games.WordleGame;
import io.github.milobotdev.milobot.main.JDAManager;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("play", "Play a game of wordle.");
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
    public void executeCommand(@NotNull SlashCommandEvent event) {
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
                userId -> WordleGame.wordleGames.get(userId).removeGame(userId)));
    }
}
