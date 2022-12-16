package io.github.milobotdev.milobot.commands.games.wordle;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordleCmdLoader {

    public static void load() {
        ExecutorService wordleExecutor = Executors.newSingleThreadExecutor();

        WordleCmd wordleParentCmd = new WordleCmd(wordleExecutor);
        wordleParentCmd.addSubCommand(new WordlePlayCmd(wordleExecutor));
        wordleParentCmd.addSubCommand(new WordleStatsCmd(wordleExecutor));
        wordleParentCmd.addSubCommand(new WordleLeaderboardCmd(wordleExecutor));

        CommandHandler.getInstance().registerCommand(wordleParentCmd);
    }
}
