package commands.games.wordle;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;

/**
 * Parent command for all Wordle sub commands.
 */
public class WordleCmd extends Command implements GamesCmd, ParentCmd {

    public WordleCmd() {
        this.commandName = "wordle";
        this.commandDescription = "Wordle brought to discord.";
        this.aliases = new String[]{"morble"};
        this.subCommands.add(new WordlePlayCmd());
        this.subCommands.add(new WordleLeaderboardCmd());
        this.subCommands.add(new WordleStatsCmd());
    }
}
