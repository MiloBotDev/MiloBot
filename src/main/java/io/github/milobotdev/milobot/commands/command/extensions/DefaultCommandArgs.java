package io.github.milobotdev.milobot.commands.command.extensions;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

/**
 * This interface provides a default implementation getCommandArgs() and checkRequiredArgs().
 * This interface should only be implemented by commands with no arguments.
 * @see TextCommand
 */
public interface DefaultCommandArgs extends TextCommand {

    @Override
    default List<String> getCommandArgs() {
        return List.of();
    }

    @Override
    default boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if (args.size() == 0) {
            return true;
        } else {
            sendInvalidArgs(event, "This command doesn't take any arguments.");
            return false;
        }
    }
}
