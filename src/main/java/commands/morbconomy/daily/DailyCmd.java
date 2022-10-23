package commands.morbconomy.daily;

import commands.Command;
import commands.ParentCmd;
import commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class DailyCmd extends Command implements MorbconomyCmd, ParentCmd {

    public DailyCmd() {
        this.commandName = "daily";
        this.commandDescription = "Collect your daily reward.";
        this.subCommands.add(new DailyStreakCmd());
        this.subCommands.add(new DailyClaimCmd());
        this.subCommands.add(new DailyStatsCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);
    }

}
