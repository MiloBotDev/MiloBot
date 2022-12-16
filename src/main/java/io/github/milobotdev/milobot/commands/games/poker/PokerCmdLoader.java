package io.github.milobotdev.milobot.commands.games.poker;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokerCmdLoader {

    public static void load() {
        ExecutorService pokerExecutor = Executors.newSingleThreadExecutor();

        PokerCmd pokerParentCmd = new PokerCmd(pokerExecutor);
        pokerParentCmd.addSubCommand(new PokerPlayCmd(pokerExecutor));
        CommandHandler.getInstance().registerCommand(pokerParentCmd);
    }
}
