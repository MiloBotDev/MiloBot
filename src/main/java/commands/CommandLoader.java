package commands;

import commands.economy.ProfileCommand;
import commands.utility.UserCommand;
import commands.games.WordleCommand;
import commands.utility.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads in every command to a static map.
 * @author Ruben Eekhof - rubeneehof@gmail.com
 */
public class CommandLoader {

    public static Map<List<String>, Command> commandList = new HashMap<>();

    public static void loadAllCommands() {
        ArrayList<Command> commands = new ArrayList<>();
        commands.add(new HelpCommand());
        commands.add(new InviteCommand());
        commands.add(new StatusCommand());
        commands.add(new PrefixCommand());
        commands.add(new UsageCommand());
        commands.add(new UserCommand());
        commands.add(new ProfileCommand());
        commands.add(new WordleCommand());

        for(Command c : commands) {
            ArrayList<String> keys = new ArrayList<>(List.of(c.aliases));
            keys.add(c.commandName);
            commandList.put(keys, c);
        }
    }

}
