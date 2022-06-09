package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Handles the incoming commands.
 * @author - Ruben Eekhof
 */
public class CommandHandler extends ListenerAdapter {

    public String prefix = "!";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        List<String> receivedMessage = Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).collect(Collectors.toList());
        AtomicBoolean commandFound = new AtomicBoolean(false);
        if(receivedMessage.get(0).startsWith(prefix)) {
            CommandLoader.commandList.keySet().stream().takeWhile(i -> !commandFound.get()).forEach(strings -> {
                if(strings.contains(receivedMessage.get(0).toLowerCase(Locale.ROOT).replace(prefix, ""))) {
                    receivedMessage.remove(0);
                    Command command = CommandLoader.commandList.get(strings);
                    // check for potential cooldown
                    if(command.cooldown > 0) {
                        boolean onCooldown = command.checkCooldown(event, command.cooldownMap);
                        if(onCooldown) {
                            return;
                        }
                    }
                    // check for flags if one or multiple arguments are present
                    if(receivedMessage.size() > 0) {
                        if(command.checkForFlags(event, receivedMessage, command.commandName, command.commandDescription,
                                command.commandArgs, command.aliases, command.flags, command.cooldown)){
                            return;
                        }
                    }
                    // execute the command
                    command.execute(event, receivedMessage);
                    // update the tracker
                    command.updateCommandTracker(command.commandName);
                    commandFound.set(true);
                }
            });
        }
    }

}
