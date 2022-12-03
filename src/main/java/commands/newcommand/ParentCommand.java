package commands.newcommand;

import commands.newcommand.extensions.SlashCommand;
import commands.newcommand.extensions.TextCommand;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentCommand extends NewCommand implements IParentCommand {
    private List<SubCommand> subCommands = new ArrayList<>();

    @Override
    public void addSubCommand(SubCommand subCommand) {
        subCommand.assignParentCommand(this);
        this.subCommands.add(subCommand);
    }

    @Override
    public List<SubCommand> getSubCommands() {
        return new ArrayList<>(subCommands);
    }

    @Override
    public String getSubCommandsText(String prefix) {
        StringBuilder subCommandsText = new StringBuilder();
        for (SubCommand subCommand : getSubCommands()) {
            subCommandsText.append("\n`").append(prefix).append(subCommand.getFullCommandName());
            if (subCommand instanceof TextCommand tSubCommand) {
                List<String> commandArgs = tSubCommand.getCommandArgs();
                for (String commandArg : commandArgs) {
                    subCommandsText.append(String.format(" {%s}", commandArg));
                }
                subCommandsText.append("`\n").append(tSubCommand.getCommandDescription());
            } else if (subCommand instanceof SlashCommand sSubCommand) {
                subCommandsText.append("`\n").append(sSubCommand.getCommandData().getDescription());
            } else {
                throw new IllegalStateException("Sub command is not a text or slash command.");
            }

        }
        return subCommandsText.toString();
    }
}
