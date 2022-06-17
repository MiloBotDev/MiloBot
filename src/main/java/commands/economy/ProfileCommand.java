package commands.economy;

import commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Profile Command.
 * Shows the user their own profile or that of someone else.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class ProfileCommand extends Command implements EconomyCommand {

    public ProfileCommand() {
        this.commandName = "profile";
        this.commandDescription = "View your own or someone else's profile.";
        this.commandArgs = new String[]{"*user"};
        this.cooldown = 60;
    }

    @Override
    public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {

    }
}
