package tk.milobot.commands.games.hungergames;

import tk.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HungerGamesCmdLoader {

    public static void load() {
        ExecutorService hungerGamesExecutor = Executors.newSingleThreadExecutor();

        HungerGamesCmd hungerGamesParentCmd = new HungerGamesCmd(hungerGamesExecutor);
        hungerGamesParentCmd.addSubCommand(new HungerGamesStartCmd(hungerGamesExecutor));
        hungerGamesParentCmd.addSubCommand(new HungerGamesStatsCmd(hungerGamesExecutor));
        hungerGamesParentCmd.addSubCommand(new HungerGamesLeaderboardCmd(hungerGamesExecutor));
        CommandHandler.getInstance().registerCommand(hungerGamesParentCmd);
    }
}
