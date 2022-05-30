package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class InviteCommand extends Command {

    public String commandName = "invite";
    public String commandDescription = "Creates an invite for the bot.";

    @Override
    public void execute(MessageReceivedEvent event, List<String> args) {
        if(checkForFlags(event, args, commandName, commandDescription, commandArgs, flags)) {return;}

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage(event.getJDA().getInviteUrl(Permission.ADMINISTRATOR)).queue();
    }
}
