package commands.newcommand;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

import java.util.Set;

public abstract class SubCommand extends NewCommand {

    public SubcommandData getCommandData() {
        throw new UnsupportedOperationException("This command does not have slash commands.");
    }
}
