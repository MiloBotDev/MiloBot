package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Usage command.
 * Shows the user statistics on how much each command has been used.
 */
public class UsageCmd extends Command implements UtilityCmd {

    public UsageCmd() {
        this.commandName = "usage";
        this.commandDescription = "See the amount of times each command has been used.";
        this.cooldown = 60;
        this.commandArgs = new String[]{"*command"};
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {

    }


}
