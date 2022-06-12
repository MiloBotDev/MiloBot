package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The prefix command.
 * Can change the prefix the bot listens to for a guild.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class PrefixCommand extends Command implements UtilityCommand {

	public PrefixCommand() {
		this.commandName = "prefix";
		this.commandDescription = "Change the prefix of the guild you're in.";
		this.commandArgs = new String[]{"prefix"};
		this.cooldown = 60;
		this.permissions.put("administrator", Permission.ADMINISTRATOR);
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, List<String> args) {

	}

}
