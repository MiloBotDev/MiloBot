package commands.games;

import commands.Command;
import games.Wordle;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * The wordle command.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class WordleCommand extends Command implements GamesCommand {

    public WordleCommand() {
        this.commandName = "wordle";
        this.commandDescription = "Try to guess the 5 letter word.";
        this.instanceTime = 300;
        this.singleInstance = true;
        this.subCommands.add(new WordleLeaderboardCommand());
    }

    @Override
    public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String authorId = event.getAuthor().getId();
        Wordle wordle = new Wordle();
        EmbedBuilder wordleEmbed = new EmbedBuilder();
        wordleEmbed.setTitle("Wordle");
        EmbedUtils.styleEmbed(event, wordleEmbed);
        StringBuilder editDescription = new StringBuilder();
        final boolean[] gameOver = {false};
        event.getChannel().sendMessageEmbeds(wordleEmbed.build()).queue(message -> {
            ListenerAdapter listener = new ListenerAdapter() {
                @Override
                public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                    if(event.getAuthor().getId().equals(authorId)) {
                        EmbedBuilder newEmbed = new EmbedBuilder();
                        newEmbed.setTitle("Wordle");
                        EmbedUtils.styleEmbed(event, newEmbed);
                        editDescription.append(wordleEmbed.getDescriptionBuilder());
                        String guess = event.getMessage().getContentRaw().toLowerCase(Locale.ROOT);
                        if(!(guess.toCharArray().length > wordle.wordLength || guess.toCharArray().length < wordle.wordLength)) {
                            String[] result = wordle.guessWord(guess);
                            editDescription.append("\u200E \u200E \u200E");
                            for(char letter : guess.toCharArray()) {
                                editDescription.append(String.format("%s", letter));
                                editDescription.append("\u200E \u200E \u200E \u200E ");
                            }
                            editDescription.append("\n");
                            for(String check : result) {
                                editDescription.append(String.format("%s ", check));
                            }
                            editDescription.append("\n");
                            if(wordle.guessed) {
                                editDescription.append("You guessed the word!");
                                event.getJDA().removeEventListener(this);
                                gameOver[0] = true;
                            } else if(wordle.guesses + 1 == wordle.maxGuesses) {
                                editDescription.append(String.format("You ran out of guesses. The correct word was: `%s`.", wordle.word));
                                event.getJDA().removeEventListener(this);
                                gameOver[0] = true;
                            }
                            newEmbed.setDescription(editDescription);
                            event.getMessage().delete().queue();
                            if(gameOver[0]) {
                                message.editMessageEmbeds(newEmbed.build()).queue(EmbedUtils.deleteEmbedButton(event, event.getAuthor().getName()));
                            } else {
                                message.editMessageEmbeds(newEmbed.build()).queue();
                            }
                        }
                    }
                }
            };
            message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener), instanceTime, TimeUnit.SECONDS);
            message.getJDA().addEventListener(listener);
        });
    }
}
