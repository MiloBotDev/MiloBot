package commands.games.blackjack;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;

public class BlackjackCmd extends Command implements ParentCmd, GamesCmd {

	public BlackjackCmd() {
		this.commandName = "blackjack";
		this.commandDescription = "Blackjack brought to discord.";
		this.aliases = new String[]{"bj"};
		this.subCommands.add(new BlackjackPlayCmd());
		this.subCommands.add(new BlackjackStatsCmd());
	}

}
