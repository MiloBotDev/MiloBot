package commands.utility;

import commands.Command;
import commands.CommandHandler;
import database.DatabaseManager;
import database.queries.PrefixTableQueries;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Change the prefix the bot listens to for a guild.
 */
public class PrefixCmd extends Command implements UtilityCmd {

	public DatabaseManager manager;

	public PrefixCmd() {
		this.commandName = "prefix";
		this.commandDescription = "Change the prefix of the guild you're in.";
		this.commandArgs = new String[]{"prefix"};
		this.cooldown = 60;
		this.permissions.put("Administrator", Permission.ADMINISTRATOR);

		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if (args.get(0).length() > 1) {
			event.getChannel().sendMessage("A prefix cant be longer then 1 character.").queue();
		} else {
			String id = event.getGuild().getId();
			this.manager.query(PrefixTableQueries.updateServerPrefix, DatabaseManager.QueryTypes.UPDATE, args.get(0), id);
			CommandHandler.prefixes.replace(id, args.get(0));
			event.getChannel().sendMessage(String.format("Prefix successfully changed to: %s", args.get(0))).queue();
		}
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		String prefix = Objects.requireNonNull(event.getOption("prefix")).getAsString();
		if(prefix.length() > 1) {
			event.reply("A prefix cant be longer then 1 character.").queue();
		} else {
			String id = Objects.requireNonNull(event.getGuild()).getId();
			this.manager.query(PrefixTableQueries.updateServerPrefix, DatabaseManager.QueryTypes.UPDATE, prefix, id);
			CommandHandler.prefixes.replace(id, prefix);
			event.reply(String.format("Prefix successfully changed to: %s", prefix)).queue();
		}
	}

	private boolean isValidPrefix() {
		return true;
	}

}
