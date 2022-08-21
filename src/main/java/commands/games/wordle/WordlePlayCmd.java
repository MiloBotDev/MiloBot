package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import games.Wordle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import newdb.dao.UserDao;
import newdb.dao.WordleDao;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
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
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        OffsetDateTime timeStarted = event.getMessage().getTimeCreated();
        String authorId = event.getAuthor().getId();
        Wordle wordle = new Wordle();
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};

        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(wordleEmbed, event.getAuthor());

        event.getChannel().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            extracted(null, timeStarted, authorId, wordle, editDescription, gameOver, wordleEmbed, message);
        });
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        OffsetDateTime timeStarted = event.getTimeCreated();
        String authorId = event.getUser().getId();
        Wordle wordle = new Wordle();
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};

        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(wordleEmbed, event.getUser());

        event.getHook().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            extracted(event, timeStarted, authorId, wordle, editDescription, gameOver, wordleEmbed, message);
        });
    }

    private void extracted(SlashCommandEvent SlashCommandEvent,
                           OffsetDateTime timeStarted, String authorId, Wordle wordle, StringBuilder editDescription,
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
                        if (!(chars.length > wordle.wordLength || chars.length < wordle.wordLength)) {
                            String[] result = wordle.guessWord(guess);
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
                            if (wordle.guessed) {
                                OffsetDateTime timeWon = event.getMessage().getTimeCreated();
                                int timeTaken = Integer.parseInt(String.valueOf(timeWon.toEpochSecond() - timeStarted.toEpochSecond()));
                                editDescription.append(String.format("You guessed the word in %d seconds. ", timeTaken));

                                event.getJDA().removeEventListener(this);
                                int fastestTime;
                                int user_id = userDao.getUserByDiscordId(event.getAuthor().getIdLong()).getId();
                                newdb.model.Wordle userWordle = wordleDao.getUserWordle(user_id);
                                if (userWordle == null) {
                                    wordleDao.addUserWordle(user_id, timeTaken, 1, 1, 1);
                                } else {
                                    if (userWordle.getFastestTime() != 0) {
                                        int currentFastestTime = Math.min(userWordle.getFastestTime(), timeTaken);
                                        if (currentFastestTime < userWordle.getFastestTime()) {
                                            editDescription.append(String.format("That's a new personal best with an improvement of %d seconds!",
                                                    userWordle.getFastestTime() - currentFastestTime));
                                        } else if (currentFastestTime == userWordle.getFastestTime()) {
                                            editDescription.append("You tied your personal best.");
                                        }
                                        fastestTime = currentFastestTime;
                                    } else {
                                        fastestTime = timeTaken;
                                    }
                                    int newStreak = userWordle.getCurrentStreak() + 1;
                                    int highestStreak = Math.max(userWordle.getHighestStreak(), newStreak);
                                    int gamesPlayed = userWordle.getGamesPlayed() + 1;
                                    int gamesWon = userWordle.getWins() + 1;
                                    int currentStreak = userWordle.getCurrentStreak() + 1;
                                    wordleDao.updateUserWordle(user_id, fastestTime, gamesWon, highestStreak, currentStreak, gamesPlayed);
                                    editDescription.append(String.format("\n**Personal Best:** %s seconds.\n", fastestTime));
                                    editDescription.append(String.format("**Current Streak:** %d games.\n", newStreak));
                                    editDescription.append(String.format("**Highest Streak:** %d games.\n", highestStreak));
                                    editDescription.append(String.format("**Total Games Played:** %d games.", gamesPlayed));
                                }
                                gameOver[0] = true;
                            } else if (wordle.guesses + 1 == wordle.maxGuesses) {
                                int user_id = userDao.getUserByDiscordId(event.getAuthor().getIdLong()).getId();
                                newdb.model.Wordle userWordle = wordleDao.getUserWordle(user_id);
                                editDescription.append(String.format("You ran out of guesses. The correct word was: `%s`.",
                                        wordle.word));
                                if (userWordle == null) {
                                    wordleDao.addUserWordle(user_id, 0, 0, 0, 0);
                                } else {
                                    int highestStreak = userWordle.getHighestStreak();
                                    int gamesPlayed = userWordle.getGamesPlayed() + 1;
                                    int fastestTime = userWordle.getFastestTime();
                                    int wins = userWordle.getWins();
                                    wordleDao.updateUserWordle(user_id, fastestTime, wins, highestStreak, 0, gamesPlayed);
                                    if (fastestTime == 0) {
                                        editDescription.append("\n**Personal Best:** not set yet.\n");
                                    } else {
                                        editDescription.append(String.format("\n**Personal Best:** %d seconds.\n", fastestTime));
                                    }
                                    editDescription.append(String.format("**Highest Streak:** %s games.\n", highestStreak));
                                    editDescription.append(String.format("**Total Games Played:** %d games.", gamesPlayed));
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
