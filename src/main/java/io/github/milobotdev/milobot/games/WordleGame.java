package io.github.milobotdev.milobot.games;

import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.dao.WordleDao;
import io.github.milobotdev.milobot.database.model.Wordle;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Representation of a Wordle game.
 */
public class WordleGame {

    final static Logger logger = LoggerFactory.getLogger(WordleGame.class);
    public final int maxGuesses = 7;
    public final int wordLength = 5;
    public String word;
    public int guesses;
    public boolean guessed;
    private ArrayList<String> words;
    public static final Map<Long, WordleGame> wordleGames = new ConcurrentHashMap<>();
    private final StringBuilder editDescription;
    private final long userId;
    private EmbedBuilder wordleEmbed;
    private final TimeTracker timeTracker;
    private final WordleDao wordleDao;
    private final UserDao userDao;
    private volatile Message message;

    public WordleGame(long userId) {
        loadWordsAsList();

        this.guesses = 0;
        this.guessed = false;
        this.word = generateWord();
        wordleGames.put(userId, this);
        this.userId = userId;
        this.wordleDao = WordleDao.getInstance();
        this.userDao = UserDao.getInstance();
        this.editDescription = new StringBuilder();
        this.timeTracker = new TimeTracker();
        this.timeTracker.start();
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            new ConcurrentHashMap<>(wordleGames).forEach((aLong, wordleGame) -> wordleGame.onMessage(event));
        }
    }

    private void onMessage(@NotNull MessageReceivedEvent event) {
        User author = event.getAuthor();
        if(author.getIdLong() == this.userId && event.getMessage().getContentRaw().toCharArray().length == this.wordLength) {
            boolean gameOver = false;
            if(this.guesses == 0) {
                this.wordleEmbed.setDescription("");
            }

            EmbedBuilder newWordleEmbed = new EmbedBuilder();
            newWordleEmbed.setTitle("Wordle");
            EmbedUtils.styleEmbed(newWordleEmbed, author);

            this.editDescription.append(this.wordleEmbed.getDescriptionBuilder());
            String guess = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
            char[] guessCharArray = guess.toCharArray();
            if (!(guessCharArray.length > this.wordLength || guessCharArray.length < this.wordLength)) {
                String[] result = this.guessWord(guess);
                this.editDescription.append("` ");
                int count = 0;
                for(char letter : guessCharArray) {
                    if (count + 1 == guessCharArray.length) {
                        this.editDescription.append(String.format("%s ", letter));
                    } else {
                        this.editDescription.append(String.format("%s  ", letter));
                    }
                    count++;
                }
                this.editDescription.append("`\n");
                for (String check : result) {
                    this.editDescription.append(String.format("%s ", check));
                }
                this.editDescription.append("\n");
            }

            if(this.guessed) {
                long timeTaken = timeTracker.getElapsedTimeSecs();
                this.editDescription.append(String.format("You guessed the word in %d seconds.", timeTaken));
                Optional<Wordle> optionalWordle = this.updateDatabase(true, (int) timeTracker.getElapsedTimeSecs(), userId);
                optionalWordle.ifPresent(wordle -> {
                    if(!(wordle.getGamesPlayed() == 1)) {
                        if(wordle.getFastestTime() == timeTaken) {
                            getPreviousFastestTime();
                            editDescription.append(String.format("\nThat's a new personal best with an improvement of %d seconds!",
                                    getPreviousFastestTime() - timeTaken));
                        } else if(!(wordle.getPreviousFastestTime() == 0)) {
                            editDescription.append("\nYou tied your personal best.");
                        }
                    }
                    editDescription.append(String.format("\n**Personal Best:** %s seconds.\n", wordle.getFastestTime()));
                    editDescription.append(String.format("**Current Streak:** %d games.\n", wordle.getCurrentStreak()));
                    editDescription.append(String.format("**Highest Streak:** %d games.\n", wordle.getHighestStreak()));
                    editDescription.append(String.format("**Total Games Played:** %d games.", wordle.getGamesPlayed()));
                });
                gameOver = true;
            } else if (this.guesses + 1 == this.maxGuesses) {
                editDescription.append(String.format("You ran out of guesses. The correct word was: `%s`.",
                        this.word));
                Optional<Wordle> optionalWordle = this.updateDatabase(false, 0, userId);
                optionalWordle.ifPresent(wordle -> {
                    if (wordle.getFastestTime() == 0) {
                        editDescription.append("\n**Personal Best:** not set yet.\n");
                    } else {
                        editDescription.append(String.format("\n**Personal Best:** %d seconds.\n", wordle.getFastestTime()));
                    }
                    editDescription.append(String.format("**Highest Streak:** %s games.\n", wordle.getHighestStreak()));
                    editDescription.append(String.format("**Total Games Played:** %d games.", wordle.getGamesPlayed()));
                });
                gameOver = true;
            }

            newWordleEmbed.setDescription(editDescription);
            event.getMessage().delete().queue();
            if(gameOver) {
                GameInstanceManager.getInstance().removeUserGame(userId);
                removeGame(userId);
                message.editMessageEmbeds(newWordleEmbed.build()).setActionRow(
                        Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
            } else {
                message.editMessageEmbeds(newWordleEmbed.build()).queue();
            }
        }
    }

    private Optional<Wordle> updateDatabase(boolean won, int timeTaken, long authorId) {
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            Wordle userWordle = this.wordleDao.getByUserDiscordId(con, authorId, RowLockType.NONE);
            // if the user has never played wordle before we need to add them to the table
            if (userWordle == null) {
                int user_id = this.userDao.getUserByDiscordId(con, authorId, RowLockType.FOR_UPDATE).getId();
                userWordle = new Wordle(user_id, 1, 1, timeTaken, 1, 1);
                this.wordleDao.add(con, userWordle);
            } else {
                userWordle.addGame(won, timeTaken);
            }
            this.wordleDao.update(con, userWordle);
            con.commit();
            return Optional.of(userWordle);
        } catch (SQLException e) {
            logger.error("Something went wrong when trying to update the wordle database ", e);
            return Optional.empty();
        }
    }

    private int getPreviousFastestTime() {
        try(Connection con = DatabaseConnection.getConnection()) {
            Wordle userWordle = this.wordleDao.getByUserDiscordId(con, userId, RowLockType.NONE);
            if(userWordle != null) {
                return userWordle.getFastestTime();
            }
            return 0;
        } catch (SQLException e) {
            logger.error("Something went wrong when trying to get the previous fastest time ", e);
            return 0;
        }
    }

    /**
     * Makes a guess for the user and checks if the guess was correct.
     *
     * @return String[] containing the result of the users guess.
     * Example:
     * [:green_square:, :green_square:, :black_large_square:, :black_large_square:, :yellow_square:]
     * A :green_square: means the letter is at the correct position.
     * A :yellow_square: means the letter exists in the word.
     * A :black_large_square: means the letter does not exist in the word.
     */
    public String[] guessWord(@NotNull String guess) {
        this.guesses++;
        char[] word = this.word.toCharArray();
        char[] guessedWord = guess.toLowerCase(Locale.ROOT).toCharArray();
        String[] result = new String[5];
        for (int i = 0; i < this.wordLength; i++) {
            // letter is in the correct position
            if (guessedWord[i] == word[i]) {
                result[i] = ":green_square:";
                // letter is in the word but in the wrong position
            } else if (this.word.indexOf(guessedWord[i]) != -1) {
                int totalYellowSquares = 0;
                int yellowSquaresBefore = 0;
                for (int j = 0; j < this.wordLength; j++) {
                    if (word[j] == guessedWord[i] && guessedWord[j] != word[j]) {
                        totalYellowSquares++;
                    }
                }
                for (int j = 0; j < i; j++) {
                    if (guessedWord[j] == guessedWord[i] && guessedWord[j] != word[j]) {
                        yellowSquaresBefore++;
                    }
                }
                if (yellowSquaresBefore < totalYellowSquares) {
                    result[i] = ":yellow_square:";
                } else {
                    result[i] = ":black_large_square:";
                }
                // letter is not in the word at all
            } else {
                result[i] = ":black_large_square:";
            }
        }
        if (guess.equals(this.word)) {
            this.guessed = true;
        }
        return result;
    }

    /**
     * Picks a random word from a list of 5-letter words.
     *
     * @return The word that was picked
     */
    private String generateWord() {
        return words.get(new Random().nextInt(words.size()));
    }

    /**
     * Loads the wordle_words.txt file into an ArrayList.
     */
    private void loadWordsAsList() {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream is = classloader.getResourceAsStream("wordle_words.txt");
        InputStreamReader streamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
        Scanner s = new Scanner(streamReader);
        ArrayList<String> list = new ArrayList<>();
        while (s.hasNext()) {
            list.add(s.next());
        }
        s.close();
        this.words = list;
    }

    public void removeGame(long userId) {
        wordleGames.remove(userId);
        this.message.delete().queue();
    }

    public void attachMessage(Message gameMessage, EmbedBuilder wordleEmbed) {
        this.message = gameMessage;
        this.wordleEmbed = wordleEmbed;
    }
}
