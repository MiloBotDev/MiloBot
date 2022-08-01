package commands.games.poker;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;

public class PokerCmd extends Command implements ParentCmd, GamesCmd {

    public PokerCmd() {
        this.commandName = "poker";
        this.commandDescription = "5-card Poker brought to discord.";
        this.subCommands.add(new PokerPlayCmd());
    }

}
