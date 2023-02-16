package io.github.milobotdev.milobot.commands.games.uno;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnoCmdLoader {

    public static void load() {
        ExecutorService unoExecutor = Executors.newSingleThreadExecutor();

        UnoCmd unoCmd = new UnoCmd(unoExecutor);
        unoCmd.addSubCommand(new UnoHostCmd(unoExecutor));
        unoCmd.addSubCommand(new UnoInfoCmd(unoExecutor));
        unoCmd.addSubCommand(new UnoLeaderboardCmd(unoExecutor));
        CommandHandler.getInstance().registerCommand(unoCmd);
    }
}
