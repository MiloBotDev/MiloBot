package tk.milobot.commands.games.uno;

import tk.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UnoCmdLoader {

    public static void load() {
        ExecutorService unoExecutor = Executors.newSingleThreadExecutor();

        UnoCmd unoCmd = new UnoCmd(unoExecutor);
        unoCmd.addSubCommand(new UnoHostCmd(unoExecutor));
        unoCmd.addSubCommand(new UnoInfoCmd(unoExecutor));
        CommandHandler.getInstance().registerCommand(unoCmd);
    }
}
