package io.github.milobotdev.milobot.commands.morbconomy;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MorbconomyCmdLoader {

    public static void load() {
        ExecutorService morbconomyExecutor = Executors.newSingleThreadExecutor();

        CommandHandler.getInstance().registerCommand(new ProfileCmd(morbconomyExecutor));

    }

}
