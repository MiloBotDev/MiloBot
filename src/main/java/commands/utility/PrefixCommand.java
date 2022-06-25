package commands.utility;

import commands.Command;
import commands.CommandHandler;
import database.DatabaseManager;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The prefix command.
 * Can change the prefix the bot listens to for a guild.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class PrefixCommand extends Command implements UtilityCommand {

	public DatabaseManager manager;

	public PrefixCommand() {
		this.commandName = "prefix";
		this.commandDescription = "Change the prefix of the guild you're in.";
		this.commandArgs = new String[]{"prefix"};
		this.cooldown = 60;
		this.permissions.put("Administrator", Permission.ADMINISTRATOR);

		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if (args.get(0).length() > 1) {
			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage("A prefix cant be longer then 1 character.").queue();
		} else {
			this.manager.query(manager.updateServerPrefix, DatabaseManager.QueryTypes.UPDATE, args.get(0), event.getGuild().getId());
			CommandHandler.prefixes.replace(event.getGuild().getId(), args.get(0));
			event.getChannel().sendMessage(String.format("Prefix successfully changed to: %s", args.get(0))).queue();
		}
	}

}
