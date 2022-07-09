package commands;

import commands.bot.bug.BugCmd;
import commands.dnd.encounter.EncounterCmd;
import commands.economy.ProfileCmd;
import commands.games.wordle.WordleCmd;
import commands.utility.*;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.Command.Choice;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Loads in every command to a static map.
 *
 * @author Ruben Eekhof - rubeneehof@gmail.com
 */
public class CommandLoader {

	public static Map<List<String>, Command> commandList = new HashMap<>();

	public static void loadAllCommands(JDA bot) {
		ArrayList<Command> commands = new ArrayList<>();
		commands.add(HelpCmd.getInstance());
		commands.add(new InviteCmd());
		commands.add(new StatusCmd());
		commands.add(new PrefixCmd());
		commands.add(new UsageCmd());
		commands.add(new UserCmd());
		commands.add(new ProfileCmd());
		commands.add(new WordleCmd());
		commands.add(new BugCmd());
		commands.add(new EncounterCmd());

		for (Command c : commands) {
			ArrayList<String> keys = new ArrayList<>(List.of(c.aliases));
			keys.add(c.commandName);
			commandList.put(keys, c);
		}


		CommandListUpdateAction slashCommands = bot.updateCommands();

		slashCommands.addCommands(Commands.slash("help", "Shows the user a list of available commands.")
						.addOption(OptionType.STRING, "command", "The command you want information about.", false))
				.queue();

		slashCommands.addCommands(Commands.slash("encounter", "D&D 5e encounter generator.")
				.addSubcommands(new SubcommandData("generate", "Generate a random encounter for the given inputs.")
						.addOptions(new OptionData(OptionType.INTEGER, "size", "The size of the party.")
									.setRequired(true)
									.setRequiredRange(1, 10))
							.addOptions(new OptionData(OptionType.INTEGER, "level", "The average level of the party.")
									.setRequired(true)
									.setRequiredRange(1, 20))
							.addOptions(new OptionData(OptionType.STRING, "difficulty", "The difficulty of the encounter.")
									.setRequired(true)
									.addChoices(new Choice("easy", "easy"), new Choice("medium", "medium"),
											new Choice("difficult", "difficult"), new Choice("deadly", "deadly")))
							.addOptions(new OptionData(OptionType.STRING, "environment", "The environment the encounter takes place in.")
									.setRequired(false)
									.addChoices(new Choice("city", "city"), new Choice("dungeon", "dungeon"),
											new Choice("forest", "forest"), new Choice("nature", "nature"),
											new Choice("other plane", "other plane"), new Choice("underground", "underground"),
											new Choice("water", "water")
									))
					)).queue();

		slashCommands.addCommands(Commands.slash("invite", "Sends an invite link to add the bot to another server.")).queue();

		slashCommands.addCommands(Commands.slash("profile", "View your own or someone else's profile.")
						.addOption(OptionType.USER, "user", "The user you want to view the profile of.", false))
				.queue();
	}

}
