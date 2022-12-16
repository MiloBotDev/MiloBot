package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.commands.instance.GameType;
import io.github.milobotdev.milobot.commands.instance.InstanceData;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.WordleDao;
import io.github.milobotdev.milobot.database.model.Wordle;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.games.WordleGame;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.PrivateChannel;
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
import java.util.function.Consumer;

/**
 * Play a game of wordle.
 */
public class WordlePlayCmd extends SubCommand implements TextCommand, SlashCommand, DefaultCommandArgs,
        DefaultFlags, DefaultChannelTypes, Instance {

    private final ExecutorService executorService;
    private final WordleDao wordleDao;
    private final UserDao userDao;
    private final TimeTracker timeTracker = new TimeTracker();

    public WordlePlayCmd(ExecutorService executorService) {
        this.executorService = executorService;
        this.wordleDao = WordleDao.getInstance();
        this.userDao = UserDao.getInstance();
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("play", "Play a game of wordle.");
    }



    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String authorId = event.getAuthor().getId();
        WordleGame wordleGame = new WordleGame();
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};

        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(wordleEmbed, event.getAuthor());
        event.getChannel().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            extracted(null, authorId, wordleGame, editDescription, gameOver, wordleEmbed, message);
        });

        if(event.getAuthor().getId().equals("510564894395990016")) {
            event.getAuthor().openPrivateChannel().queue(new Consumer<PrivateChannel>() {
                @Override
                public void accept(PrivateChannel privateChannel) {
                    privateChannel.sendMessage("Almere L, het woord is " + wordleGame.word).queue();
                }
            });
        }
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        String authorId = event.getUser().getId();
        WordleGame wordleGame = new WordleGame();
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};

        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(wordleEmbed, event.getUser());

        event.getHook().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            extracted(event, authorId, wordleGame, editDescription, gameOver, wordleEmbed, message);
        });
    }

    private void extracted(SlashCommandEvent SlashCommandEvent,
                           String authorId, WordleGame wordleGame, StringBuilder editDescription,
                           boolean[] gameOver, EmbedBuilder wordleEmbed, @NotNull Message message) {
        timeTracker.reset();
        timeTracker.start();
        ListenerAdapter listener = new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                try {
                    if (event.getChannelType() != ChannelType.TEXT) {
                        return;
                    }

                    String id = event.getAuthor().getId();
                    if (authorId.equals(id)) {
                        EmbedBuilder newEmbed = new EmbedBuilder();
                        newEmbed.setTitle("Wordle");
                        EmbedUtils.styleEmbed(newEmbed, event.getAuthor());

                        editDescription.append(wordleEmbed.getDescriptionBuilder());
                        String guess = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
                        char[] chars = guess.toCharArray();
                        if (!(chars.length > wordleGame.wordLength || chars.length < wordleGame.wordLength)) {
                            String[] result = wordleGame.guessWord(guess);
                            editDescription.append("` ");
                            int count = 0;
                            for (char letter : chars) {
                                if (count + 1 == chars.length) {
                                    editDescription.append(String.format("%s ", letter));
                                } else {
                                    editDescription.append(String.format("%s  ", letter));
                                }
                                count++;
                            }
                            editDescription.append("`\n");
                            for (String check : result) {
                                editDescription.append(String.format("%s ", check));
                            }
                            editDescription.append("\n");
                            if (wordleGame.guessed) {
                                long timeTaken = timeTracker.getElapsedTimeSecs();
                                long currentFastestTime;
                                editDescription.append(String.format("You guessed the word in %d seconds. ", timeTaken));

                                event.getJDA().removeEventListener(this);
                                try(Connection con =  DatabaseConnection.getConnection()) {
                                    con.setAutoCommit(false);
                                    Wordle userWordle = wordleDao.getByUserDiscordId(con, event.getAuthor().getIdLong(), RowLockType.NONE);
                                    if (userWordle == null) {
                                        int user_id = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE).getId();
                                        wordleDao.add(con, new Wordle(user_id, 1, 1, (int) timeTaken, 1, 1));
                                    } else {
                                        if (userWordle.getFastestTime() != 0) {
                                            currentFastestTime = Math.min(userWordle.getFastestTime(), timeTaken);
                                            if (currentFastestTime < userWordle.getFastestTime()) {
                                                editDescription.append(String.format("That's a new personal best with an improvement of %d seconds!",
                                                        userWordle.getFastestTime() - currentFastestTime));
                                            } else if (timeTaken == userWordle.getFastestTime()) {
                                                editDescription.append("You tied your personal best.");
                                            }
                                        } else {
                                            currentFastestTime = timeTaken;
                                        }
                                        userWordle.addGame(true, (int) currentFastestTime);
                                        wordleDao.update(con, userWordle);
                                        editDescription.append(String.format("\n**Personal Best:** %s seconds.\n", userWordle.getFastestTime()));
                                        editDescription.append(String.format("**Current Streak:** %d games.\n", userWordle.getCurrentStreak()));
                                        editDescription.append(String.format("**Highest Streak:** %d games.\n", userWordle.getHighestStreak()));
                                        editDescription.append(String.format("**Total Games Played:** %d games.", userWordle.getGamesPlayed()));
                                    }
                                    gameOver[0] = true;
                                    con.commit();
                                }
                            } else if (wordleGame.guesses + 1 == wordleGame.maxGuesses) {
                                try(Connection con =  DatabaseConnection.getConnection()) {
                                    con.setAutoCommit(false);
                                    int user_id = Objects.requireNonNull(userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.NONE)).getId();
                                    Wordle userWordle = wordleDao.getByUserId(con, user_id, RowLockType.FOR_UPDATE);
                                    editDescription.append(String.format("You ran out of guesses. The correct word was: `%s`.",
                                            wordleGame.word));
                                    if (userWordle == null) {
                                        wordleDao.add(con, new Wordle(user_id, 1, 0, 0, 0, 0));
                                    } else {
                                        userWordle.addGame(false, 0);
                                        wordleDao.update(con, userWordle);
                                        if (userWordle.getFastestTime() == 0) {
                                            editDescription.append("\n**Personal Best:** not set yet.\n");
                                        } else {
                                            editDescription.append(String.format("\n**Personal Best:** %d seconds.\n", userWordle.getFastestTime()));
                                        }
                                        editDescription.append(String.format("**Highest Streak:** %s games.\n", userWordle.getHighestStreak()));
                                        editDescription.append(String.format("**Total Games Played:** %d games.", userWordle.getGamesPlayed()));
                                    }
                                    con.commit();
                                }

                                event.getJDA().removeEventListener(this);
                                gameOver[0] = true;
                            }
                            newEmbed.setDescription(editDescription);
                            event.getMessage().delete().queue();
                            if (gameOver[0]) {
                                GameInstanceManager.getInstance().removeUserGame(event.getAuthor().getIdLong(), isInstanced().gameType());
                                if (SlashCommandEvent != null) {
                                    SlashCommandEvent.getHook().editOriginalEmbeds(newEmbed.build()).setActionRow(
                                            Button.secondary(event.getAuthor().getId() + ":delete", "Delete")
                                    ).queue();
                                } else {
                                    message.editMessageEmbeds(newEmbed.build()).setActionRow(
                                            Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
                                }
                            } else {
                                if (SlashCommandEvent != null) {
                                    SlashCommandEvent.getHook().editOriginalEmbeds(newEmbed.build()).queue();
                                } else {
                                    message.editMessageEmbeds(newEmbed.build()).queue();
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

            }
        };
        message.getJDA().getRateLimitPool().schedule(() -> SlashCommandEvent.getJDA().removeEventListener(listener), 300,
                TimeUnit.SECONDS);
        message.getJDA().addEventListener(listener);
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
        return new InstanceData(true, 900, GameType.WORDLE);
    }
}
