package io.github.milobotdev.milobot.commands.command;

import io.github.milobotdev.milobot.commands.command.extensions.SlashCommand;
import io.github.milobotdev.milobot.commands.command.extensions.TextCommand;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutorService;

public interface ICommand {

    @NotNull String getCommandName();
    @NotNull String getCommandDescription();
    @NotNull
    default String getFullCommandName() {
        if (this instanceof ParentCommand) {
            return getCommandName();
        } else if (this instanceof SubCommand subCommand) {
            ParentCommand parentCommand = subCommand.getParentCommand();
            if (parentCommand instanceof TextCommand tParentCommand) {
                return String.format("%s %s", tParentCommand.getCommandName(), getCommandName());
            } else if (parentCommand instanceof SlashCommand sParentCommand) {
                return String.format("%s %s", sParentCommand.getCommandData().getName(), getCommandName());
            } else {
                throw new IllegalStateException("Parent command is not a text or slash command.");
            }
        } else {
            throw new IllegalStateException("This command is not a parent or sub command.");
        }
    }

    @NotNull ExecutorService getExecutorService();
}
