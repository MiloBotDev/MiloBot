package commands.games.wordle;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import database.queries.WordleTableQueries;
import games.Wordle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Play a game of wordle.
 */
public class WordlePlayCmd extends Command implements SubCmd {

	private final DatabaseManager manager;

	public WordlePlayCmd() {
		this.commandName = "play";
		this.commandDescription = "Play a game of wordle.";
		this.instanceTime = 300;
		this.singleInstance = true;
		this.manager = DatabaseManager.getInstance();
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
						   boolean[] gameOver, EmbedBuilder wordleEmbed, Message message) {
		ListenerAdapter listener = new ListenerAdapter() {
			@Override
			public void onMessageReceived(@NotNull MessageReceivedEvent event) {
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
							if(count + 1 == chars.length) {
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
							String timeTaken = String.valueOf(timeWon.toEpochSecond() - timeStarted.toEpochSecond());

							event.getJDA().removeEventListener(this);
							ArrayList<String> resultQuery = manager.query(WordleTableQueries.selectUserWordle,
									DatabaseManager.QueryTypes.RETURN, id);
							editDescription.append(String.format("You guessed the word in %s seconds. ", timeTaken));
							if (resultQuery.size() == 0) {
								manager.query(WordleTableQueries.addUserWordle, DatabaseManager.QueryTypes.UPDATE, id,
										timeTaken, "true", "1", "1", "1");
							} else {
								int timeTakenAsInt = Integer.parseInt(timeTaken);
								String currentFastestTime;
								if (!resultQuery.get(1).equals("null")) {
									int previousFastestTime = Integer.parseInt(resultQuery.get(1));
									int currentFastestTimeInt = Math.min(timeTakenAsInt, previousFastestTime);
									currentFastestTime = String.valueOf(currentFastestTimeInt);
									if (timeTakenAsInt < previousFastestTime) {
										editDescription.append(String.format("That's a new personal best with an improvement of %d seconds!",
												previousFastestTime - timeTakenAsInt));
									} else if (timeTakenAsInt == previousFastestTime) {
										editDescription.append("You tied your personal best.");
									}
								} else {
									currentFastestTime = timeTaken;
								}
								int newStreak = Integer.parseInt(resultQuery.get(3)) + 1;
								int highestStreak = Integer.parseInt(resultQuery.get(5));
								int newTotalGames = Integer.parseInt(resultQuery.get(4)) + 1;
								int newHighestStreak = Math.max(highestStreak, newStreak);
								manager.query(WordleTableQueries.updateUserWordle, DatabaseManager.QueryTypes.UPDATE,
										currentFastestTime, "true", String.valueOf(newStreak),
										String.valueOf(newTotalGames), String.valueOf(newHighestStreak), authorId);
								editDescription.append(String.format("\n**Personal Best:** %s seconds.\n", currentFastestTime));
								editDescription.append(String.format("**Current Streak:** %d games.\n", newStreak));
								editDescription.append(String.format("**Highest Streak:** %d games.\n", newHighestStreak));
								editDescription.append(String.format("**Total Games Played:** %d games.", newTotalGames));
							}
							gameOver[0] = true;
						} else if (wordle.guesses + 1 == wordle.maxGuesses) {
							ArrayList<String> resultQuery = manager.query(WordleTableQueries.selectUserWordle,
									DatabaseManager.QueryTypes.RETURN, id);
							editDescription.append(String.format("You ran out of guesses. The correct word was: `%s`.",
									wordle.word));
							if (resultQuery.size() == 0) {
								manager.query(WordleTableQueries.addUserWordle, DatabaseManager.QueryTypes.UPDATE, id,
										"null", "false", "0", "1", "0");
							} else {
								String highestStreak = resultQuery.get(5);
								int newTotalGames = Integer.parseInt(resultQuery.get(4)) + 1;
								manager.query(WordleTableQueries.updateUserWordle, DatabaseManager.QueryTypes.UPDATE,
										String.valueOf(resultQuery.get(1)), "false", "0",
										String.valueOf(newTotalGames), highestStreak, authorId);
								String previousFastestTime = resultQuery.get(1);
								if (previousFastestTime.equals("null")) {
									editDescription.append("\n**Personal Best:** not set yet.\n");
								} else {
									editDescription.append(String.format("\n**Personal Best:** %s seconds.\n", previousFastestTime));
								}
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
							if(SlashCommandEvent != null) {
								SlashCommandEvent.getHook().editOriginalEmbeds(newEmbed.build()).setActionRow(
										Button.secondary(event.getAuthor().getId() + ":delete", "Delete")
								).queue();
							} else {
								message.editMessageEmbeds(newEmbed.build()).setActionRow(
										Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
							}
						} else {
							if(SlashCommandEvent != null) {
								SlashCommandEvent.getHook().editOriginalEmbeds(newEmbed.build()).queue();
							} else {
								message.editMessageEmbeds(newEmbed.build()).queue();
							}
						}
					}
				}
			}
		};
		message.getJDA().getRateLimitPool().schedule(() -> SlashCommandEvent.getJDA().removeEventListener(listener), instanceTime,
				TimeUnit.SECONDS);
		message.getJDA().addEventListener(listener);
	}
}
