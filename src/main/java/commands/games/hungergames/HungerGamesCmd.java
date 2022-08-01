package commands.games.hungergames;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;

public class HungerGamesCmd extends Command implements ParentCmd, GamesCmd {

    public HungerGamesCmd() {
        this.commandName = "hungergames";
        this.aliases = new String[]{"hg"};
        this.commandDescription = "Hunger Games";
        this.subCommands.add(new HungerGamesStartCmd());
    }
}
