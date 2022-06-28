package commands.games.wordle;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Parent command for all Wordle sub commands.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class WordleCmd extends Command implements GamesCmd, ParentCmd {

	public WordleCmd() {
		this.commandName = "wordle";
		this.commandDescription = "Play a game of wordle or view the leaderboards.";
		this.aliases = new String[]{"morble"};
		this.subCommands.add(new WordlePlayCmd());
		this.subCommands.add(new WordleLeaderboardCmd());

	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {

	}
}
