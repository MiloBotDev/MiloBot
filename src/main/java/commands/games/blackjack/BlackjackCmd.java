package commands.games.blackjack;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class BlackjackCmd extends Command implements ParentCmd, GamesCmd {

    public BlackjackCmd() {
        this.commandName = "blackjack";
        this.commandDescription = "Blackjack brought to discord.";
        this.aliases = new String[]{"bj"};
        this.subCommands.add(new BlackjackPlayCmd());
        this.subCommands.add(new BlackjackStatsCmd());
        this.subCommands.add(new BlackjackInfoCmd());
        this.subCommands.add(new BlackjackLeaderboardCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);
    }

}
