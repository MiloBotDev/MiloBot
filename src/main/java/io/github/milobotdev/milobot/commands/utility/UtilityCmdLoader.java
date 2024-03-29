package io.github.milobotdev.milobot.commands.utility;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UtilityCmdLoader {

    public static void load() {
        ExecutorService utilityExecutor = Executors.newSingleThreadExecutor();

        CommandHandler commandHandler = CommandHandler.getInstance();
        commandHandler.registerCommand(new HelpCmd(utilityExecutor));
        commandHandler.registerCommand(new InviteCmd(utilityExecutor));
        commandHandler.registerCommand(new PrefixCmd(utilityExecutor));
        commandHandler.registerCommand(new ServerCmd(utilityExecutor));
        commandHandler.registerCommand(new UserCmd(utilityExecutor));
        commandHandler.registerCommand(new UsageCmd(utilityExecutor));
    }
}
