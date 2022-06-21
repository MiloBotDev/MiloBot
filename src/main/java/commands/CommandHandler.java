package commands;

import database.DatabaseManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import user.User;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Handles the incoming commands.
 * @author - Ruben Eekhof
 */
public class CommandHandler extends ListenerAdapter {

    private final static Logger logger = LoggerFactory.getLogger(CommandHandler.class);

    private final User user;
    private final DatabaseManager manager;

    public final static HashMap<String, String> prefixes = new HashMap<>();

    public CommandHandler() {
        this.user = User.getInstance();
        this.manager = DatabaseManager.getInstance();
        // loads all the prefixes into a map
        ArrayList<String> query = manager.query(manager.getAllPrefixes, DatabaseManager.QueryTypes.RETURN);
        for(int i=0; i < query.size(); i+=2) {
            prefixes.put(query.get(i), query.get(i + 1));
            if(i == query.size()) {
                break;
            }
        }
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        // ignore messages from other bots or itself
        if(event.getMessage().getAuthor().isBot()) {
            return;
        }

        String guildId = event.getGuild().getId();
        List<String> receivedMessage = Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).collect(Collectors.toList());
        AtomicBoolean commandFound = new AtomicBoolean(false);
        String prefix = prefixes.get(guildId);
        // check if the message starts with the prefix
        if(receivedMessage.get(0).startsWith(prefix)) {
            CommandLoader.commandList.keySet().stream().takeWhile(i -> !commandFound.get()).forEach(strings -> {
                if(receivedMessage.size() == 0) {
                    return;
                }
                if(strings.contains(receivedMessage.get(0).toLowerCase(Locale.ROOT).replaceFirst(prefix, ""))) {
                    receivedMessage.remove(0);
                    Command command = CommandLoader.commandList.get(strings);
                    // check if the user is calling to a subcommand of this command
                    if(receivedMessage.size() > 0) {
                        for(Command subCommand : command.subCommands) {
                            if(receivedMessage.get(0).toLowerCase(Locale.ROOT).equals(subCommand.commandName)) {
                                receivedMessage.remove(0);
                                command = subCommand;
                                break;
                            }
                        }
                    }
                    // check for flags if one or multiple arguments are present
                    if(receivedMessage.size() > 0) {
                        if(command.checkForFlags(event, receivedMessage, command.commandName, command.commandDescription,
                                command.commandArgs, command.aliases, command.flags, command.cooldown, command.subCommands)){
                            return;
                        }
                    }
                    // check if the author has the required permissions
                    if(!command.checkRequiredPermissions(event, command.permissions)) {
                        command.sendMissingPermissions(event, command.commandName, command.permissions, prefix);
                        return;
                    }
                    // check if all required args are present
                    if(command.calculateRequiredArgs(command.commandArgs) > receivedMessage.size()) {
                        command.sendCommandUsage(event, command.commandName, command.commandArgs);
                        return;
                    }
                    // check for potential cooldown
                    if(command.cooldown > 0) {
                        boolean onCooldown = command.checkCooldown(event, command.cooldownMap);
                        if(onCooldown) {
                            return;
                        }
                    }
                    // check for single instance
                    if(command.singleInstance) {
                        boolean instanceOpen = command.checkInstanceOpen(event, command.gameInstanceMap, command.commandName);
                        if(instanceOpen) {
                            return;
                        }
                    }
                    // execute the command
                    command.execute(event, receivedMessage);
                    logger.info(String.format("Executed command: %s | Author: %s.", command.commandName,
                            event.getAuthor().getName()));
                    // update the tracker
                    command.updateCommandTracker(command.commandName);
                    String userId = event.getAuthor().getId();
                    command.updateCommandTrackerUser(command.commandName, userId);
                    // check if this user exists in the database otherwise add it
                    if(!user.checkIfUserExists(userId)) {
                        manager.query(manager.addUser, DatabaseManager.QueryTypes.UPDATE, userId,
                                "0", "1", "0");
                    }
                    user.updateExperience(userId, 50, event);

                    // stops the loop
                    commandFound.set(true);
                }
            });
        }
    }

}
