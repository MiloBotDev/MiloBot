package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The invite command.
 * Sends the user an invite link, so they can invite the bot to their own server.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class InviteCmd extends Command implements UtilityCmd {

	public InviteCmd() {
		this.commandName = "invite";
		this.commandDescription = "Creates an invite for the bot.";
		this.aliases = new String[]{"inv"};
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessage(event.getJDA().getInviteUrl(Permission.ADMINISTRATOR)).queue();
	}
}

