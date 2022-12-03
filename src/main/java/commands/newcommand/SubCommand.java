package commands.newcommand;

import commands.newcommand.extensions.TextCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Set;

public abstract class SubCommand extends NewCommand implements ISubCommand {
    private ParentCommand parentCommand = null;

    void assignParentCommand(ParentCommand parentCommand) {
        if (this.parentCommand == null) {
            this.parentCommand = parentCommand;
        } else {
            throw new IllegalArgumentException("Parent command already assigned.");
        }
    }

    @Override
    public ParentCommand getParentCommand() {
        return parentCommand;
    }
}
