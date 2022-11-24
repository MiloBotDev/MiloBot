package commands.newcommand;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

import java.util.Set;

public abstract class ParentCommand extends NewCommand {

    public CommandData getCommandData() {
        throw new UnsupportedOperationException("This command does not have slash commands.");
    }
}
