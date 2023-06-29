package io.github.milobotdev.milobot.commands.games.minesweeper;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MinesweeperCmdLoader {

    public static void load() {
        ExecutorService minesweeperExecutor = Executors.newSingleThreadExecutor();

        MinesweeperCmd minesweeperCmd = new MinesweeperCmd(minesweeperExecutor);
        minesweeperCmd.addSubCommand(new MinesweeperPlayCmd(minesweeperExecutor));
        CommandHandler.getInstance().registerCommand(minesweeperCmd);
    }
}
