package games;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Scanner;

/**
 * Representation of a Wordle game.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Wordle {

	final static Logger logger = LoggerFactory.getLogger(Wordle.class);

	public final int maxGuesses = 7;
	public final int wordLength = 5;

	public String word;
	public int guesses;
	public boolean guessed;

	private ArrayList<String> words;

	public Wordle() {
		loadWordsAsList();

		this.guesses = 0;
		this.guessed = false;
		this.word = generateWord();
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
				int charInWordAmount = 0;
				int amountGuessed = 0;
				for (char c : word) {
					if (c == guessedWord[i]) {
						charInWordAmount++;
					}
				}
				for (char guessedLetter : guessedWord) {
					if (guessedWord[i] == guessedLetter) {
						amountGuessed++;
					}
				}
				if (!(amountGuessed > charInWordAmount)) {
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
	 * Picks a random word from a list of 5 letter words.
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
		try {
			Scanner s = new Scanner(new File(Config.getInstance().wordleWordsPath));
			ArrayList<String> list = new ArrayList<>();
			while (s.hasNext()) {
				list.add(s.next());
			}
			s.close();
			this.words = list;
		} catch (FileNotFoundException e) {
			logger.info("wordle_words.txt not found.");
			logger.error(e.getMessage());
		}
	}
}
