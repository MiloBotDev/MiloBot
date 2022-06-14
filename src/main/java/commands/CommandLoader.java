package commands;

import commands.fun.UserCommand;
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
        // utility commands
        HelpCommand helpCommand = new HelpCommand();
        ArrayList<String> helpCommandKeys = new ArrayList<>(List.of(helpCommand.aliases));
        helpCommandKeys.add(helpCommand.commandName);
        commandList.put(helpCommandKeys, helpCommand);

        InviteCommand inviteCommand = new InviteCommand();
        ArrayList<String> inviteCommandKeys = new ArrayList<>(List.of(inviteCommand.aliases));
        inviteCommandKeys.add(inviteCommand.commandName);
        commandList.put(inviteCommandKeys, inviteCommand);

        StatusCommand statusCommand = new StatusCommand();
        ArrayList<String> statusCommandKeys = new ArrayList<>(List.of(statusCommand.aliases));
        statusCommandKeys.add(statusCommand.commandName);
        commandList.put(statusCommandKeys, statusCommand);

        PrefixCommand prefixCommand = new PrefixCommand();
        ArrayList<String> prefixCommandKeys = new ArrayList<>(List.of(prefixCommand.aliases));
        prefixCommandKeys.add(prefixCommand.commandName);
        commandList.put(prefixCommandKeys, prefixCommand);

        UsageCommand usageCommand = new UsageCommand();
        ArrayList<String> usageCommandKeys = new ArrayList<>(List.of(usageCommand.aliases));
        usageCommandKeys.add(usageCommand.commandName);
        commandList.put(usageCommandKeys, usageCommand);

        // fun commands
        UserCommand userCommand = new UserCommand();
        ArrayList<String> userCommandKeys = new ArrayList<>(List.of(userCommand.aliases));
        userCommandKeys.add(userCommand.commandName);
        commandList.put(userCommandKeys, userCommand);
    }

}
