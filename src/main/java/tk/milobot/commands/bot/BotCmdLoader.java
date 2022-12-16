package tk.milobot.commands.bot;

import tk.milobot.commands.CommandHandler;
import tk.milobot.commands.bot.bug.BugCmd;
import tk.milobot.commands.bot.bug.BugListCmd;
import tk.milobot.commands.bot.bug.BugReportCmd;
import tk.milobot.commands.bot.bug.BugViewCmd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotCmdLoader {

    public static void load() {
        ExecutorService statusExecutor = Executors.newSingleThreadExecutor();
        CommandHandler.getInstance().registerCommand(new StatusCmd(statusExecutor));

        ExecutorService bugExecutor = Executors.newSingleThreadExecutor();
        CommandHandler.getInstance().registerCommand(new BugCmd(bugExecutor)
                .addSubCommand(new BugListCmd(bugExecutor))
                .addSubCommand(new BugReportCmd(bugExecutor))
                .addSubCommand(new BugViewCmd(bugExecutor)));
    }
}
