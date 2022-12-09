package tk.milobot.commands.games.wordle;

import tk.milobot.commands.Command;
import tk.milobot.commands.ParentCmd;
import tk.milobot.commands.games.GamesCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

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
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }
}
