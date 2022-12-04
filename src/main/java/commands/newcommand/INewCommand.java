package commands.newcommand;

import commands.newcommand.extensions.SlashCommand;
import commands.newcommand.extensions.TextCommand;

public interface INewCommand {

    String getCommandName();
    String getCommandDescription();
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
}
