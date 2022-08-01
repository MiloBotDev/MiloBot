package commands.games.hungergames;

import commands.Command;
import commands.SubCmd;

import java.util.HashMap;
import java.util.Map;

public class HungerGamesStartCmd extends Command implements SubCmd {

    public static Map<String, String> hungerGamesInstances = new HashMap<>();

    public HungerGamesStartCmd() {
        this.commandName = "start";
        this.aliases = new String[]{"s", "host"};
        this.commandDescription = "Starts the Hunger Games";
    }
}

