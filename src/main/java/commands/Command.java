package commands;

import database.DatabaseManager;
import database.queries.CommandTrackerTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic implementation of a command.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public abstract class Command {

	/**
	 * The name of the command.
	 */
	public String commandName = "commandName";

	/**
	 * The description of the command.
	 */
	public String commandDescription = "The description for this command";

	/**
	 * The arguments the command has.
	 */
	public String[] commandArgs = {};

	/**
	 * The different flags the command can be flagged with.
	 */
	public String[] flags = {"--help", "--stats"};

	/**
	 * The aliases this command has.
	 */
	public String[] aliases = {};

	/**
	 * The time needed to call the command again in seconds.
	 */
	public int cooldown = 0;

	/**
	 * Determines if the user can have multiple instances of the same command running.
	 */
	public boolean singleInstance = false;

	/**
	 * The amount of time an instance will be open for.
	 */
	public int instanceTime = 0;

	/**
	 * The permissions needed to use the command.
	 */
	public HashMap<String, Permission> permissions = new HashMap<>();

	/**
	 * A map that maps a users name to time when they can use the command again.
	 */
	public HashMap<String, OffsetDateTime> cooldownMap = new HashMap<>();

	/**
	 * A map that maps a users' id to the time when they can open another game instance.
	 */
	public HashMap<String, OffsetDateTime> gameInstanceMap = new HashMap<>();

	/**
	 * A list of all the sub commands this command has.
	 */
	public ArrayList<Command> subCommands = new ArrayList<>();

	/**
	 * The default constructor for a command.
	 */
	public Command() {

	}

	/**
	 * The default implementation for every command.
	 */
	public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
		event.getChannel().sendMessage("This command has not yet been implemented.").queue();
	}

	/**
	 * The default implementation for every slash command.
	 */
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		event.reply("This command has not yet been implemented.").queue();
	}

	/**
	 * The default implementation for checking if a flag is present.
	 *
	 * @return true if a flag was present, false if no flag was present.
	 */
	public boolean checkForFlags(MessageReceivedEvent event, @NotNull List<String> args, String commandName,
								 String commandDescription, String[] commandArgs, String[] aliases, String[] flags,
								 int cooldown, ArrayList<Command> subCommands) {
		boolean flagPresent = false;
		// checks if --help flag is present as an argument
		if (args.contains("--help")) {
			EmbedBuilder embedBuilder = generateHelp(commandName, commandDescription, commandArgs, aliases, flags, cooldown, subCommands,
					event.getGuild(), event.getAuthor());
			event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
					Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
			flagPresent = true;
		}
		// checks if the --stats flag is present as an argument
		if (args.contains("--stats")) {
			generateStats(event, commandName);
			flagPresent = true;
		}
		return flagPresent;
	}

	/**
	 * Checks if the user is using the command again before the cooldown is over.
	 *
	 * @return true if the command is on cooldown, false if otherwise
	 */
	public boolean checkCooldown(@NotNull MessageReceivedEvent event, @NotNull HashMap<String, OffsetDateTime> cooldownMap) {
		OffsetDateTime currentTime = event.getMessage().getTimeCreated();
		String authorId = event.getAuthor().getId();
		OffsetDateTime newAvailableTime = event.getMessage().getTimeCreated().plusSeconds(cooldown);
		if (cooldownMap.containsKey(authorId)) {
			OffsetDateTime availableTime = cooldownMap.get(authorId);
			if (currentTime.isBefore(availableTime)) {
				long waitTime = availableTime.toEpochSecond() - currentTime.toEpochSecond();
				event.getChannel().sendTyping().queue();
				event.getChannel().sendMessage(String.format("You can use this command again in %d seconds.", waitTime))
						.queue();
				return true;
			} else {
				cooldownMap.remove(authorId);
			}
		} else {
			cooldownMap.put(authorId, newAvailableTime);
		}
		return false;
	}

	/**
	 * Checks if the user is using the command again when its only allowed to have 1 instance open.
	 *
	 * @return true if the command already has an instance open, false if otherwise
	 */
	public boolean checkInstanceOpen(@NotNull MessageReceivedEvent event, @NotNull HashMap<String, OffsetDateTime> gameInstanceMap, String commandName) {
		OffsetDateTime currentTime = event.getMessage().getTimeCreated();
		String authorId = event.getAuthor().getId();
		OffsetDateTime newAvailableTime = event.getMessage().getTimeCreated().plusSeconds(instanceTime);
		if (gameInstanceMap.containsKey(authorId)) {
			OffsetDateTime availableTime = gameInstanceMap.get(authorId);
			if (currentTime.isBefore(availableTime)) {
				long waitTime = availableTime.toEpochSecond() - currentTime.toEpochSecond();
				event.getChannel().sendTyping().queue();
				event.getChannel().sendMessage(String.format("You can only have 1 %s game open at the same time. " +
								"Finish your %s game or wait %d seconds.", commandName, commandName, waitTime))
						.queue();
				return true;
			} else {
				gameInstanceMap.remove(authorId);
			}
		} else {
			gameInstanceMap.put(authorId, newAvailableTime);
		}
		return false;
	}

	/**
	 * Updates the command tracker for a specific user;
	 */
	public void updateCommandTrackerUser(String commandName, String userId) {
		DatabaseManager manager = DatabaseManager.getInstance();
		ArrayList<String> query = manager.query(CommandTrackerTableQueries.checkIfCommandUsageUserTracked, DatabaseManager.QueryTypes.RETURN, commandName, userId);
		if (query.size() == 0) {
			manager.query(CommandTrackerTableQueries.addCommandUsageUserToTracker, DatabaseManager.QueryTypes.UPDATE, commandName, userId, "1");
		} else {
			ArrayList<String> result = manager.query(CommandTrackerTableQueries.checkCommandUsageUserAmount, DatabaseManager.QueryTypes.RETURN, commandName, userId);
			String newAmount = Integer.toString(Integer.parseInt(result.get(0)) + 1);
			manager.query(CommandTrackerTableQueries.updateCommandUsageUserAmount, DatabaseManager.QueryTypes.UPDATE, newAmount, commandName, userId);
		}
	}

	/**
	 * Generates a message with the stats of that specific command.
	 */
	public void generateStats(@NotNull MessageReceivedEvent event, String commandName) {
		DatabaseManager manager = DatabaseManager.getInstance();
		ArrayList<String> personalAmount = manager.query(CommandTrackerTableQueries.checkCommandUsageUserAmount, DatabaseManager.QueryTypes.RETURN, commandName, event.getAuthor().getId());

		EmbedBuilder stats = new EmbedBuilder();
		EmbedUtils.styleEmbed(stats, event.getAuthor());
		stats.setTitle(String.format("Stats for %s", commandName));
		stats.addField("Personal Usages", String.format("You have used this command %d times.", Integer.parseInt(personalAmount.get(0))), false);

		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessageEmbeds(stats.build()).setActionRow(
				Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
	}

	/**
	 * Generates a standard help message for when the command is called with the --help flag.
	 */
	public EmbedBuilder generateHelp(String commandName, String commandDescription, String @NotNull [] commandArgs,
									 String @NotNull [] aliases, String[] flags, int cooldown,
									 @NotNull ArrayList<Command> subCommands, @NotNull Guild guild, @NotNull User author) {
		String prefix = CommandHandler.prefixes.get(guild.getId());
		String consumerId = author.getId();

		EmbedBuilder info = new EmbedBuilder();
		EmbedUtils.styleEmbed(info, author);
		info.setTitle(commandName);
		info.setDescription(commandDescription);

		StringBuilder argumentsText = getArgumentsText(commandName, commandArgs, prefix);
		info.addField(
				"Usage",
				argumentsText.toString(),
				false
		);

		if (!(subCommands.size() == 0)) {
			StringBuilder subCommandsText = getSubCommandsText(commandName, subCommands, prefix);
			info.addField("Sub Commands", subCommandsText.toString(), false);
		}

		if (!(aliases.length == 0)) {
			StringBuilder aliasesText = new StringBuilder();
			for (int i = 0; i < aliases.length; i++) {
				aliasesText.append('`').append(aliases[i]).append('`');
				if (!(i + 1 == flags.length)) {
					aliasesText.append(", ");
				}
			}
			info.addField("Aliases", aliasesText.toString(), false);
		}

		if (!(flags.length == 0)) {
			StringBuilder flagsText = new StringBuilder();
			for (int i = 0; i < flags.length; i++) {
				flagsText.append('`').append(flags[i]).append('`');
				if (!(i + 1 == flags.length)) {
					flagsText.append(", ");
				}
			}
			info.addField("Flags", flagsText.toString(), false);
		}

		if (cooldown > 0) {
			info.addField("Cooldown", String.format("%d seconds.", cooldown), false);
		}

		if (!permissions.isEmpty()) {
			StringBuilder permissionsText = new StringBuilder();
			permissions.forEach((s, permission) -> permissionsText.append("`").append(s).append("`"));
			info.addField("Permissions", permissionsText.toString(), false);
		}

		return info;
	}

	/**
	 * Builds a String that explains the sub commands a command has.
	 *
	 * @return the String as a StringBuilder instance.
	 */
	@NotNull
	private StringBuilder getSubCommandsText(String commandName, @NotNull ArrayList<Command> subCommands, String prefix) {
		StringBuilder subCommandsText = new StringBuilder();
		for (Command subCommand : subCommands) {
			subCommandsText.append("\n`").append(prefix).append(String.format("%s ", commandName)).append(subCommand.commandName);
			if (!(subCommand.commandArgs.length == 0)) {
				for (int y = 0; y < subCommand.commandArgs.length; y++) {
					subCommandsText.append(String.format(" {%s}", subCommand.commandArgs[y]));
				}
			}
			subCommandsText.append("`\n").append(subCommand.commandDescription);
		}
		return subCommandsText;
	}

	/**
	 * Builds a String that explains the usage of a command.
	 *
	 * @return the String as a StringBuilder instance
	 */
	@NotNull
	private StringBuilder getArgumentsText(String commandName, String @NotNull [] commandArgs, String prefix) {
		StringBuilder argumentsText = new StringBuilder();
		if (commandArgs.length == 0) {
			argumentsText.append("`").append(prefix).append(commandName).append("`");
		} else {
			argumentsText.append("`").append(prefix).append(commandName).append(" ");
			for (int i = 0; i < commandArgs.length; i++) {
				argumentsText.append("{").append(commandArgs[i]).append("}");
				if (!(i + 1 == commandArgs.length)) {
					argumentsText.append(" ");
				}
			}
			argumentsText.append('`');
			argumentsText.append("\n Arguments marked with * are optional, " +
					"arguments marked with ** accept multiple inputs.");
		}
		return argumentsText;
	}

	/**
	 * Generates and sends a message for when the command has been improperly used.
	 */
	public void sendCommandUsage(@NotNull MessageReceivedEvent event, String commandName, String @NotNull [] commandArgs) {
		String prefix = CommandHandler.prefixes.get(event.getGuild().getId());
		String consumerId = event.getAuthor().getId();

		EmbedBuilder info = new EmbedBuilder();
		EmbedUtils.styleEmbed(info, event.getAuthor());
		info.setTitle("Missing required arguments");
		info.setDescription(getArgumentsText(commandName, commandArgs, prefix));

		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessageEmbeds(info.build()).setActionRow(
				Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
	}

	/**
	 * Generates and sends a message for when the parent command has been called without a sub command.
	 */
	public void sendCommandExplanation(@NotNull MessageReceivedEvent event, String commandName,
									   @NotNull ArrayList<Command> subCommands, String prefix) {
		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, event.getAuthor());
		embed.setTitle(commandName);
		embed.setDescription("This is the base command for all wordle related commands. Please use any of the " +
				"commands listed below.");
		embed.addField("Sub Commands", getSubCommandsText(commandName, subCommands, prefix).toString(), false);
		event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
				Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
	}

	/**
	 * Calculates the required amount of arguments a command has.
	 *
	 * @return the required amount of arguments
	 */
	public int calculateRequiredArgs(String @NotNull [] commandArgs) {
		int requiredArgs = 0;
		for (String commandArg : commandArgs) {
			if (!commandArg.contains("*")) {
				requiredArgs += 1;
			}
		}
		return requiredArgs;
	}

	/**
	 * Checks if the user has the permissions required to use the command.
	 *
	 * @return true if the user has the required permissions, false otherwise.
	 */
	public boolean checkRequiredPermissions(@NotNull MessageReceivedEvent event, @NotNull HashMap<String, Permission> permissions) {
		AtomicBoolean hasPermission = new AtomicBoolean(false);
		Member member = event.getMember();
		if (member == null) {
			hasPermission.set(false);
			return hasPermission.get();
		}
		if (permissions.isEmpty()) {
			hasPermission.set(true);
		} else {
			permissions.forEach((s, p) -> {
				hasPermission.set(member.getPermissions().contains(p));
			});
		}
		return hasPermission.get();
	}

	/**
	 * Generates and sends a message for when a user is missing the required permissions to use the command.
	 */
	public void sendMissingPermissions(@NotNull MessageReceivedEvent event, String commandName,
									   @NotNull HashMap<String, Permission> permissions, String prefix) {
		Member member = event.getMember();
		ArrayList<String> missingPermissions = new ArrayList<>();
		if (member == null) {
			// this should never happen
			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessage("Something went wrong. We're sorry about that").queue();
			return;
		}
		EnumSet<Permission> memberPermissions = member.getPermissions();
		permissions.forEach((s, p) -> {
			if (!(memberPermissions.contains(p))) {
				missingPermissions.add(s);
			}
		});
		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, event.getAuthor());
		embed.setTitle(String.format("Missing required permissions for: %s%s", prefix, commandName));
		StringBuilder missingPermissionsText = new StringBuilder();
		missingPermissionsText.append("You are missing the following permission(s): ");
		for (int i = 0; i < missingPermissions.size(); i++) {
			missingPermissionsText.append("`").append(missingPermissions.get(i)).append("`");
			if (i + 1 == missingPermissions.size()) {
				missingPermissionsText.append(".");
			} else {
				missingPermissionsText.append(", ");
			}
		}
		embed.setDescription(missingPermissionsText.toString());
		event.getChannel().sendTyping().queue();
		event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
				Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
	}

}