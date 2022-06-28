package commands.bot.bug;

import commands.Command;
import commands.ParentCmd;
import commands.bot.BotCmd;

/**
 * Parent command for all Bug sub commands.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class BugCmd extends Command implements BotCmd, ParentCmd {

	public BugCmd() {
		this.commandName = "bug";
		this.commandDescription = "Add bugs to the bots issue tracker, or view them.";
		this.subCommands.add(new BugReportCmd());
		this.subCommands.add(new BugViewCmd());
		this.subCommands.add(new BugListCmd());
	}

}
