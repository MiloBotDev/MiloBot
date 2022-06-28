package commands;

import commands.bot.bug.BugCmd;
import commands.economy.ProfileCmd;
import commands.games.wordle.WordleCmd;
import commands.utility.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads in every command to a static map.
 *
 * @author Ruben Eekhof - rubeneehof@gmail.com
 */
public class CommandLoader {

	public static Map<List<String>, Command> commandList = new HashMap<>();

	public static void loadAllCommands() {
		ArrayList<Command> commands = new ArrayList<>();
		commands.add(new HelpCmd());
		commands.add(new InviteCmd());
		commands.add(new StatusCmd());
		commands.add(new PrefixCmd());
		commands.add(new UsageCmd());
		commands.add(new UserCmd());
		commands.add(new ProfileCmd());
		commands.add(new WordleCmd());
		commands.add(new BugCmd());

		for (Command c : commands) {
			ArrayList<String> keys = new ArrayList<>(List.of(c.aliases));
			keys.add(c.commandName);
			commandList.put(keys, c);
		}
	}

}
