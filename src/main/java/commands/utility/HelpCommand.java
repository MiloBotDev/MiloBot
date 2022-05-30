package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The Help command.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class HelpCommand extends Command {

    public String commandName = "help";
    public String commandDescription = "Shows the user a list of available commands.";
    public String[] commandArgs = {"*command"};

    @Override
    public void execute(@NotNull MessageReceivedEvent event, List<String> args) {
        if(checkForFlags(event, args, commandName, commandDescription, commandArgs, flags)) {return;}

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage("Standard help command").queue();
    }

}
