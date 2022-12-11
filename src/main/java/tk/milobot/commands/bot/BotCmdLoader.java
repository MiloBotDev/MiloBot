package tk.milobot.commands.bot;

import tk.milobot.commands.NewCommandHandler;
import tk.milobot.commands.bot.bug.NewBugCmd;
import tk.milobot.commands.bot.bug.NewBugListCmd;
import tk.milobot.commands.bot.bug.NewBugReportCmd;
import tk.milobot.commands.bot.bug.NewBugViewCmd;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BotCmdLoader {

    public static void load() {
        ExecutorService statusExecutor = Executors.newSingleThreadExecutor();
        NewCommandHandler.getInstance().registerCommand(new NewStatusCmd(statusExecutor));

        ExecutorService bugExecutor = Executors.newSingleThreadExecutor();
        NewCommandHandler.getInstance().registerCommand(new NewBugCmd(bugExecutor)
                .addSubCommand(new NewBugListCmd(bugExecutor))
                .addSubCommand(new NewBugReportCmd(bugExecutor))
                .addSubCommand(new NewBugViewCmd(bugExecutor)));
    }
}
