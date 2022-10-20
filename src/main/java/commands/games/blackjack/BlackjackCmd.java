package commands.games.blackjack;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;
import net.dv8tion.jda.api.entities.ChannelType;

public class BlackjackCmd extends Command implements ParentCmd, GamesCmd {

    public BlackjackCmd() {
        this.commandName = "blackjack";
        this.commandDescription = "Blackjack brought to discord.";
        this.aliases = new String[]{"bj"};
        this.subCommands.add(new BlackjackPlayCmd());
        this.subCommands.add(new BlackjackStatsCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.subCommands.add(new BlackjackInfoCmd());
        this.subCommands.add(new BlackjackLeaderboardCmd());
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = "blackjack");
    }

}
