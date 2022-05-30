package commands;

import commands.utility.HelpCommand;
import commands.utility.InviteCommand;

import java.util.*;

/**
 * Loads in every command to a static map.
 * @author Ruben Eekhof - rubeneehof@gmail.com
 */
public class CommandLoader {

    static Map<List<String>, Command> commandList = new HashMap<>();

    public static void loadAllCommands() {
        commandList.put(new ArrayList<>(List.of("help")), new HelpCommand());
        commandList.put(new ArrayList<>(List.of("invite", "inv")), new InviteCommand());
    }

}
