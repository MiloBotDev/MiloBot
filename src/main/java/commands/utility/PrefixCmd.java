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
import java.util.Locale;
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
		this.cooldown = 0;
		this.permissions.put("Administrator", Permission.ADMINISTRATOR);

		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String prefix = args.get(0);
		if (prefix.length() > 2) {
			event.getChannel().sendMessage("A prefix cant be longer then 2 characters.").queue();
		} else {
			if(isValidPrefix(prefix)) {
				String id = event.getGuild().getId();
				this.manager.query(PrefixTableQueries.updateServerPrefix, DatabaseManager.QueryTypes.UPDATE, prefix, id);
				CommandHandler.prefixes.replace(id, prefix);
				event.getChannel().sendMessage(String.format("Prefix successfully changed to: %s", prefix)).queue();
			} else {
				event.getChannel().sendMessage(String.format("`%s` is not a valid prefix", prefix)).queue();
			}
		}
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		String prefix = Objects.requireNonNull(event.getOption("prefix")).getAsString();
		if(prefix.length() > 2) {
			event.reply("A prefix cant be longer then 2 characters.").queue();
		} else {
			if(isValidPrefix(prefix)) {
				String id = Objects.requireNonNull(event.getGuild()).getId();
				this.manager.query(PrefixTableQueries.updateServerPrefix, DatabaseManager.QueryTypes.UPDATE, prefix, id);
				CommandHandler.prefixes.replace(id, prefix);
				event.reply(String.format("Prefix successfully changed to: %s", prefix)).queue();
			} else {
				event.reply(String.format("`%s` is not a valid prefix", prefix)).queue();
			}
		}
	}

	private boolean isValidPrefix(@NotNull String prefix) {
		return !prefix.toLowerCase(Locale.ROOT).contains("*");
	}

}
