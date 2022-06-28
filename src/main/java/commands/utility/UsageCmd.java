package commands.utility;

import commands.Command;
import database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * The Usage command.
 * Shows the user statistics on how much each command has been used.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class UsageCmd extends Command implements UtilityCmd {

	public final DatabaseManager manager;

	public UsageCmd() {
		this.commandName = "usage";
		this.commandDescription = "See the amount of times each command has been used.";
		this.cooldown = 60;
		this.commandArgs = new String[]{"*command"};

		this.manager = DatabaseManager.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		event.getChannel().sendTyping().queue();
		if (args.size() == 0) {
			ArrayList<String> result = manager.query(manager.getAllCommandUsages, DatabaseManager.QueryTypes.RETURN);
			int counter = 0;
			StringBuilder usages = new StringBuilder();
			for (int i = 0; i < result.size(); i += 2) {
				if (!(i + 2 > result.size())) {
					usages.append("`").append(result.get(i)).append("`: ").append(result.get(i + 1)).append(" times.\n");
				}
				counter++;
			}
			EmbedBuilder embed = new EmbedBuilder();
			EmbedUtils.styleEmbed(event, embed);
			embed.setTitle("Command Usages");
			embed.setDescription(usages.toString());
			event.getChannel().sendMessageEmbeds(embed.build()).queue(EmbedUtils.deleteEmbedButton(event, event.getAuthor().getId()));
		}
	}


}
