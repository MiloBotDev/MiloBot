package commands.morbconomy.daily;

import commands.Command;
import commands.ParentCmd;
import commands.morbconomy.MorbconomyCmd;

public class DailyCmd extends Command implements MorbconomyCmd, ParentCmd {

    public DailyCmd() {
        this.commandName = "daily";
        this.commandDescription = "Collect your daily reward.";
        this.subCommands.add(new DailyStreakCmd());
        this.subCommands.add(new DailyClaimCmd());
        this.subCommands.add(new DailyStatsCmd());
    }

}
