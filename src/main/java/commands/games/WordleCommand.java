package commands.games;

import commands.Command;
import database.DatabaseManager;
import games.Wordle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The wordle command.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class WordleCommand extends Command implements GamesCommand {

	private final DatabaseManager manager;

	public WordleCommand() {
		this.commandName = "wordle";
		this.commandDescription = "Try to guess the 5 letter word.";
		this.instanceTime = 300;
		this.singleInstance = true;
		this.subCommands.add(new WordleLeaderboardCommand());
		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		OffsetDateTime timeStarted = event.getMessage().getTimeCreated();
		String authorId = event.getAuthor().getId();
		Wordle wordle = new Wordle();
		StringBuilder editDescription = new StringBuilder();
		final boolean[] gameOver = {false};

		EmbedBuilder wordleEmbed = new EmbedBuilder();
		wordleEmbed.setTitle("Wordle");
		EmbedUtils.styleEmbed(event, wordleEmbed);

		event.getChannel().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
			ListenerAdapter listener = new ListenerAdapter() {
				@Override
				public void onMessageReceived(@NotNull MessageReceivedEvent event) {
					String id = event.getAuthor().getId();
					if (authorId.equals(id)) {
						EmbedBuilder newEmbed = new EmbedBuilder();
						newEmbed.setTitle("Wordle");
						EmbedUtils.styleEmbed(event, newEmbed);

						editDescription.append(wordleEmbed.getDescriptionBuilder());
						String guess = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
						if (!(guess.toCharArray().length > wordle.wordLength || guess.toCharArray().length < wordle.wordLength)) {
							String[] result = wordle.guessWord(guess);
							editDescription.append("\u200E \u200E \u200E");
							for (char letter : guess.toCharArray()) {
								editDescription.append(String.format("%s", letter));
								editDescription.append("\u200E \u200E \u200E \u200E ");
							}
							editDescription.append("\n");
							for (String check : result) {
								editDescription.append(String.format("%s ", check));
							}
							editDescription.append("\n");
							if (wordle.guessed) {
								OffsetDateTime timeWon = event.getMessage().getTimeCreated();
								String timeTaken = String.valueOf(timeWon.toEpochSecond() - timeStarted.toEpochSecond());

								event.getJDA().removeEventListener(this);
								ArrayList<String> resultQuery = manager.query(manager.selectUserWordle,
										DatabaseManager.QueryTypes.RETURN, id);
								editDescription.append(String.format("You guessed the word in %s seconds. ", timeTaken));
								if (resultQuery.size() == 0) {
									manager.query(manager.addUserWordle, DatabaseManager.QueryTypes.UPDATE, id,
											timeTaken, "true", "1", "1", "1");
								} else {
									int previousFastestTime = Integer.parseInt(resultQuery.get(1));
									int newStreak = Integer.parseInt(resultQuery.get(3)) + 1;
									int highestStreak = Integer.parseInt(resultQuery.get(5));
									int timeTakenAsInt = Integer.parseInt(timeTaken);
									int currentFastestTime = Math.min(timeTakenAsInt, previousFastestTime);
									int newTotalGames = Integer.parseInt(resultQuery.get(4)) + 1;
									int newHighestStreak = Math.max(highestStreak, newStreak);
									if (timeTakenAsInt < previousFastestTime) {
										editDescription.append(String.format("That's a new personal best with an improvement of %d seconds!",
												previousFastestTime - timeTakenAsInt));
									} else if (timeTakenAsInt == previousFastestTime) {
										editDescription.append("You tied your personal best.");
									}
									manager.query(manager.updateUserWordle, DatabaseManager.QueryTypes.UPDATE,
											String.valueOf(currentFastestTime), "true", String.valueOf(newStreak),
											String.valueOf(newTotalGames), String.valueOf(newHighestStreak), authorId);
									editDescription.append(String.format("\n**Personal Best:** %d seconds.\n", currentFastestTime));
									editDescription.append(String.format("**Current Streak:** %d games.\n", newStreak));
									editDescription.append(String.format("**Highest Streak:** %d games.\n", newHighestStreak));
									editDescription.append(String.format("**Total Games Played:** %d games.", newTotalGames));
								}
								gameOver[0] = true;
							} else if (wordle.guesses + 1 == wordle.maxGuesses) {
								ArrayList<String> resultQuery = manager.query(manager.selectUserWordle,
										DatabaseManager.QueryTypes.RETURN, id);
								editDescription.append(String.format("You ran out of guesses. The correct word was: `%s`.",
										wordle.word));
								if (resultQuery.size() == 0) {
									manager.query(manager.addUserWordle, DatabaseManager.QueryTypes.UPDATE, id,
											"null", "false", "0", "1", "0");
								} else {
									String highestStreak = resultQuery.get(5);
									int newTotalGames = Integer.parseInt(resultQuery.get(4)) + 1;
									manager.query(manager.updateUserWordle, DatabaseManager.QueryTypes.UPDATE,
											String.valueOf(resultQuery.get(1)), "false", "0",
											String.valueOf(newTotalGames), highestStreak, authorId);
									int previousFastestTime = Integer.parseInt(resultQuery.get(1));
									editDescription.append(String.format("\n**Personal Best:** %d seconds.\n", previousFastestTime));
									editDescription.append(String.format("**Highest Streak:** %s games.\n", highestStreak));
									editDescription.append(String.format("**Total Games Played:** %d games.", newTotalGames));
								}
								event.getJDA().removeEventListener(this);
								gameOver[0] = true;
							}
							newEmbed.setDescription(editDescription);
							event.getMessage().delete().queue();
							if (gameOver[0]) {
								gameInstanceMap.remove(id);
								message.editMessageEmbeds(newEmbed.build()).queue(EmbedUtils.deleteEmbedButton(event,
										event.getAuthor().getName()));
							} else {
								message.editMessageEmbeds(newEmbed.build()).queue();
							}
						}
					}
				}
			};
			message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener), instanceTime,
					TimeUnit.SECONDS);
			message.getJDA().addEventListener(listener);
		});
	}
}
