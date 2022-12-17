package io.github.milobotdev.milobot.commands.morbconomy.daily;

import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyCmdLoader {

    public static void load() {
        ExecutorService dailyService = Executors.newSingleThreadExecutor();

        DailyCmd dailyCmd = new DailyCmd(dailyService);
        dailyCmd.addSubCommand(new DailyClaimCmd(dailyService));
        dailyCmd.addSubCommand(new DailyStatsCmd(dailyService));
        dailyCmd.addSubCommand(new DailyStreakCmd(dailyService));
        CommandHandler.getInstance().registerCommand(dailyCmd);
    }
}
