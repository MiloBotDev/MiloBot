package commands.newcommand;

import commands.newcommand.extensions.TextCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Set;

public abstract class SubCommand extends NewCommand {
    private ParentCommand parentCommand = null;

    public SubcommandData getCommandData() {
        throw new UnsupportedOperationException("This command does not have slash commands.");
    }

    void assignParentCommand(ParentCommand parentCommand) {
        if (this.parentCommand == null) {
            this.parentCommand = parentCommand;
        } else {
            throw new IllegalArgumentException("Parent command already assigned.");
        }
    }

    public ParentCommand getParentCommand() {
        return parentCommand;
    }
}
