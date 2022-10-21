package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import games.WordleGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import database.dao.UserDao;
import database.dao.WordleDao;
import database.model.Wordle;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Play a game of wordle.
 */
public class WordlePlayCmd extends Command implements SubCmd {

    private final WordleDao wordleDao;
    private final UserDao userDao;

    public WordlePlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of wordle.";
        this.instanceTime = 300;
        this.singleInstance = true;
        this.wordleDao = WordleDao.getInstance();
        this.userDao = UserDao.getInstance();
        this.slashSubcommandData = new SubcommandData(this.commandName, this.commandDescription);
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        OffsetDateTime timeStarted = event.getMessage().getTimeCreated();
        String authorId = event.getAuthor().getId();
        WordleGame wordleGame = new WordleGame();
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};

        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(wordleEmbed, event.getAuthor());

        event.getChannel().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            extracted(null, timeStarted, authorId, wordleGame, editDescription, gameOver, wordleEmbed, message);
        });
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        OffsetDateTime timeStarted = event.getTimeCreated();
        String authorId = event.getUser().getId();
        WordleGame wordleGame = new WordleGame();
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};

        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(wordleEmbed, event.getUser());

        event.getHook().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            extracted(event, timeStarted, authorId, wordleGame, editDescription, gameOver, wordleEmbed, message);
        });
    }

    private void extracted(SlashCommandEvent SlashCommandEvent,
                           OffsetDateTime timeStarted, String authorId, WordleGame wordleGame, StringBuilder editDescription,
                           boolean[] gameOver, EmbedBuilder wordleEmbed, @NotNull Message message) {
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
                                OffsetDateTime timeWon = event.getMessage().getTimeCreated();
                                int timeTaken = Integer.parseInt(String.valueOf(timeWon.toEpochSecond() - timeStarted.toEpochSecond()));
                                editDescription.append(String.format("You guessed the word in %d seconds. ", timeTaken));

                                event.getJDA().removeEventListener(this);
                                int fastestTime = timeTaken;
                                try(Connection con =  DatabaseConnection.getConnection()) {
                                    con.setAutoCommit(false);
                                    Wordle userWordle = wordleDao.getByUserDiscordId(con, event.getAuthor().getIdLong(), RowLockType.NONE);
                                    if (userWordle == null) {
                                        int user_id = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE).getId();
                                        wordleDao.add(con, new Wordle(user_id, 1, 1, timeTaken, 1, 1));
                                    } else {
                                        if (userWordle.getFastestTime() != 0) {
                                            int currentFastestTime = Math.min(userWordle.getFastestTime(), timeTaken);
                                            fastestTime = currentFastestTime;
                                            if (currentFastestTime < userWordle.getFastestTime()) {
                                                editDescription.append(String.format("That's a new personal best with an improvement of %d seconds!",
                                                        userWordle.getFastestTime() - currentFastestTime));
                                            } else if (currentFastestTime == userWordle.getFastestTime()) {
                                                editDescription.append("You tied your personal best.");
                                            }
                                        }
                                        userWordle.addGame(true, fastestTime);
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
                                gameInstanceMap.remove(id);
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
        message.getJDA().getRateLimitPool().schedule(() -> SlashCommandEvent.getJDA().removeEventListener(listener), instanceTime,
                TimeUnit.SECONDS);
        message.getJDA().addEventListener(listener);
    }
}
