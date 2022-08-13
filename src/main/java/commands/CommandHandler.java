package commands;

import database.DatabaseManager;
import database.queries.PrefixTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import newdb.dao.UserDao;
import newdb.dao.UserDaoImplementation;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.User;

import java.awt.*;
import java.sql.SQLException;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Handles the incoming commands.
 */
public class CommandHandler extends ListenerAdapter {

	public final static HashMap<String, String> prefixes = new HashMap<>();
	private final static Logger logger = LoggerFactory.getLogger(CommandHandler.class);
	private final User user;
	private final DatabaseManager manager;
	private final UserDao userDao = UserDaoImplementation.getInstance();

	public CommandHandler() {
		this.user = User.getInstance();
		this.manager = DatabaseManager.getInstance();
		// loads all the prefixes into a map
		ArrayList<String> query = manager.query(PrefixTableQueries.getAllPrefixes, DatabaseManager.QueryTypes.RETURN);
		for (int i = 0; i < query.size(); i += 2) {
			prefixes.put(query.get(i), query.get(i + 1));
			if (i == query.size()) {
				break;
			}
		}
	}

	@Override
	public void onMessageReceived(@NotNull MessageReceivedEvent event) {
		if (event.getChannelType() != ChannelType.TEXT) {
			return;
		}

		// ignore messages from other bots or itself
		if (event.getMessage().getAuthor().isBot()) {
			return;
		}

		String guildId = event.getGuild().getId();
		List<String> receivedMessage = Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
				.map(String::toLowerCase).collect(Collectors.toList());
		AtomicBoolean commandFound = new AtomicBoolean(false);
		String prefix = prefixes.get(guildId);
		// check if the message starts with the prefix
		if (receivedMessage.get(0).startsWith(prefix)) {
			CommandLoader.commandList.keySet().stream().takeWhile(i -> !commandFound.get()).forEach(strings -> {
				if (receivedMessage.size() == 0) {
					return;
				}
				if (strings.contains(receivedMessage.get(0).toLowerCase(Locale.ROOT).replaceFirst(prefix, ""))) {
					// stops the loop
					commandFound.set(true);

					receivedMessage.remove(0);
					Command command = CommandLoader.commandList.get(strings);
					String fullCommandName = command.commandName;
					// check if the user is calling to a subcommand of this command
					if (receivedMessage.size() > 0) {
						for (Command subCommand : command.subCommands) {
							if (receivedMessage.get(0).toLowerCase(Locale.ROOT).equals(subCommand.commandName)) {
								receivedMessage.remove(0);
								command = subCommand;
								fullCommandName += String.format(" %s", subCommand.commandName);
								break;
							}
						}
					}
					// check for flags if one or multiple arguments are present
					if (receivedMessage.size() > 0) {
						if (command.checkForFlags(event, receivedMessage, fullCommandName, command.commandDescription,
								command.commandArgs, command.aliases, command.flags, command.cooldown, command.subCommands)) {
							return;
						}
					}
					// check if the author has the required permissions
					if (!command.checkRequiredPermissions(event, command.permissions)) {
						command.sendMissingPermissions(event, command.commandName, command.permissions, prefix);
						return;
					}
					// check if the command is a parent command
					if (command instanceof ParentCmd) {
						command.sendCommandExplanation(event, command.commandName, command.subCommands, prefix);
						return;
					}
					// check if all required args are present
					if (command.calculateRequiredArgs(command.commandArgs) > receivedMessage.size()) {
						command.sendCommandUsage(event, command.commandName, command.commandArgs);
						return;
					}
					// check for potential cooldown
					if (command.cooldown > 0) {
						boolean onCooldown = command.checkCooldown(event, command.cooldownMap);
						if (onCooldown) {
							return;
						}
					}
					// check for single instance
					if (command.singleInstance) {
						boolean instanceOpen = command.checkInstanceOpen(event, command.gameInstanceMap, command.commandName);
						if (instanceOpen) {
							return;
						}
					}
					// update the tracker
					String userId = event.getAuthor().getId();
					command.updateCommandTrackerUser(fullCommandName, userId);
					// check if this user exists in the database otherwise add it
					if (!user.checkIfUserExists(event.getAuthor().getIdLong())) {
						try {
							addUserToDatabase(event.getAuthor());
						} catch (SQLException e) {
							logger.error("Couldn't add user to database", e);
							return;
						}
					}
					try {
						user.updateExperience(event.getAuthor().getIdLong(), 10, event.getAuthor().getAsMention(),
								event.getChannel());
					} catch (SQLException e) {
						logger.error("Couldn't update user experience", e);
					}
					// execute the command
					command.executeCommand(event, receivedMessage);
					logger.info(String.format("Executed command: %s | Author: %s.", fullCommandName,
							event.getAuthor().getName()));
				}
			});
		}
	}

	@Override
	public void onSlashCommand(@NotNull SlashCommandEvent event) {
		// send error message for commands not from a guild (commands from DMs)
		if (event.getGuild() == null) {
			EmbedBuilder eb = new EmbedBuilder();
			eb.setColor(Color.RED);
			eb.setTitle("Error");
			eb.setDescription("Commands cannot be used in DMs.");
			event.replyEmbeds(eb.build()).queue();
			return;
		}
		AtomicBoolean commandFound = new AtomicBoolean(false);
		CommandLoader.commandList.keySet().stream().takeWhile(i -> !commandFound.get()).forEach(strings -> {
			if (strings.contains(event.getName()) || strings.contains(event.getSubcommandName())) {
				commandFound.set(true);
				Command command = CommandLoader.commandList.get(strings);
				String fullCommandName = command.commandName;
				if(event.getSubcommandName() != null) {
					for (Command subCommand : command.subCommands) {
						if (event.getSubcommandName().toLowerCase(Locale.ROOT).equals(subCommand.commandName)) {
							command = subCommand;
							fullCommandName += String.format(" %s", subCommand.commandName);
							break;
						}
					}
				} else {
					command = CommandLoader.commandList.get(strings);
				}
				String prefix = prefixes.get(event.getGuild().getId());
				if (!command.checkRequiredPermissions(event, command.permissions)) {
					command.sendMissingPermissions(event, command.commandName, command.permissions, prefix);
					return;
				}
				if (!user.checkIfUserExists(event.getUser().getIdLong())) {
					try {
						addUserToDatabase(event.getUser());
					} catch (SQLException e) {
						logger.error("Couldn't add user to database", e);
						return;
					}
				}
				command.executeSlashCommand(event);
				command.updateCommandTrackerUser(fullCommandName, event.getUser().getId());
				try {
					user.updateExperience(event.getUser().getIdLong(), 10, event.getUser().getAsMention(),
							event.getChannel());
				} catch (SQLException e) {
					logger.error("Couldn't update user experience", e);
				}
			}
		});

	}

	private void addUserToDatabase(net.dv8tion.jda.api.entities.User user) throws SQLException {
		newdb.model.User newUser = new newdb.model.User(user.getIdLong());
		userDao.add(newUser);
	}

}
