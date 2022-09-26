package commands;

import database.dao.DailyDao;
import database.dao.UserDao;
import database.model.Daily;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;
import utility.Users;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class NewCommandHandler extends ListenerAdapter {
    private record CommandRecord(Command command, ExecutorService executor) {
    }
    private final Map<String, CommandRecord> commands = new HashMap<>();
    public final Map<Long, String> prefixes = new HashMap<>();
    private final UserDao userDao = UserDao.getInstance();
    private final DailyDao dailyDao = DailyDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(NewCommandHandler.class);
    private final JDA jda;

    public NewCommandHandler (JDA jda) {
        this.jda = jda;
    }

    public void registerCommand(ExecutorService service, Command command) {
        commands.put(command.commandName, new CommandRecord(command, service));
        // add slash sub command to jda and check for null
        if (command.slashCommandData != null) {
            CommandData commandData = command.slashCommandData;
            for (Command subCommand : command.subCommands) {
                if (subCommand.slashCommandData != null) {
                    commandData.addSubcommands(subCommand.slashSubcommandData);
                }
            }
            jda.updateCommands().addCommands(commandData).queue();
        }
    }

    public void initialize() {
        // register event listener
        jda.addEventListener(this);
        jda.addEventListener((EventListener) genericEvent -> commands.forEach((name, record) -> {
            record.command.listeners.forEach(listener -> record.executor.submit(() -> listener.onEvent(genericEvent)));
            record.command.subCommands.forEach(subCommand ->
                    subCommand.listeners.forEach(listener -> record.executor.submit(() -> listener.onEvent(genericEvent))));
        }));
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        String prefix;
        if (event.isFromGuild()) {
            long guildId = event.getGuild().getIdLong();
            if (!prefixes.containsKey(guildId)) {
                prefixes.put(guildId, Config.getInstance().getDefaultPrefix());
            }

            prefix = prefixes.get(guildId);
        } else if (event.getChannelType() == ChannelType.PRIVATE) {
            prefix = Config.getInstance().getPrivateChannelPrefix();
        } else {
            prefix = Config.getInstance().getDefaultPrefix();
        }
        if (!message.startsWith(prefix)) {
            return;
        }
        message = message.substring(prefix.length());

        List<String> messageParts = new ArrayList<>(Arrays.stream(message.split("\\s+"))
                .map(String::toLowerCase).toList());
        if (messageParts.size() == 0) {
            return;
        }

        CommandRecord commandRecord = commands.get(messageParts.get(0).toLowerCase());
        if (commandRecord == null) {
            return;
        }

        ExecutorService executorService = commandRecord.executor;
        messageParts.remove(0);
        executorService.submit(() -> {
            Command command = commandRecord.command;
            String fullCommandName = command.commandName;

            if (messageParts.size() > 0) {
                for (Command subCommand : command.subCommands) {
                    if (subCommand.commandName.equals(messageParts.get(0).toLowerCase())) {
                        messageParts.remove(0);
                        command = subCommand;
                        fullCommandName += " " + subCommand.commandName;
                        break;
                    }
                }
            }

            if (!command.checkChannelAllowed(event.getChannel().getType())) {
                command.sendInvalidChannel(event);
                return;
            }

            // check for flags if one or multiple arguments are present
            if (messageParts.size() > 0) {
                if (command.checkForFlags(event, messageParts)) {
                    return;
                }
            }
            // check if the author has the required permissions
            if (!command.checkRequiredPermissions(event)) {
                command.sendMissingPermissions(event, prefix);
                return;
            }
            // check if the command is a parent command
            if (command instanceof ParentCmd) {
                command.sendCommandExplanation(event, prefix);
                return;
            }
            // check if all required args are present
            if (command.calculateRequiredArgs() > messageParts.size()) {
                command.sendCommandUsage(event);
                return;
            }
            // check for potential cooldown
            if (command.cooldown > 0) {
                boolean onCooldown = command.checkCooldown(event);
                if (onCooldown) {
                    return;
                }
            }
            // check for single instance
            if (command.singleInstance) {
                boolean instanceOpen = command.checkInstanceOpen(event);
                if (instanceOpen) {
                    return;
                }
            }
            // check if this user exists in the database otherwise add it
            if (!Users.getInstance().checkIfUserExists(event.getAuthor().getIdLong())) {
                try {
                    addUserToDatabase(event.getAuthor());
                } catch (SQLException e) {
                    logger.error("Couldn't add user to database", e);
                    return;
                }
            }
            // update the tracker
            long userId = event.getAuthor().getIdLong();
            command.updateCommandTrackerUser(userId);
            try {
                Users.getInstance().updateExperience(event.getAuthor().getIdLong(), 10, event.getAuthor().getAsMention(),
                        event.getChannel());
            } catch (SQLException e) {
                logger.error("Couldn't update user experience", e);
            }
            // execute the command
            Command finalCommand = command;
            executorService.submit(() -> finalCommand.executeCommand(event, messageParts));
            logger.trace(String.format("Executed command: %s | Author: %s.", fullCommandName,
                    event.getAuthor().getName()));
        });
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
        if (event.getUser().isBot()) {
            return;
        }
        String commandName = event.getName();
        CommandRecord commandRecord = commands.get(commandName);
        if (commandRecord == null) {
            return;
        }

        ExecutorService executorService = commandRecord.executor;
        executorService.submit(() -> {
            Command command = commandRecord.command;
            String fullCommandName = command.commandName;
            if (event.getSubcommandName() != null) {
                for (Command subCommand : command.subCommands) {
                    if (subCommand.commandName.equals(event.getSubcommandName())) {
                        command = subCommand;
                        fullCommandName += " " + subCommand.commandName;
                        break;
                    }
                }
            }
            if (!command.checkChannelAllowed(event.getChannelType())) {
                command.sendInvalidChannel(event);
                return;
            }
            if (!command.checkRequiredPermissions(event)) {
                command.sendMissingPermissions(event, "");
                return;
            }
            // check for potential cooldown
            if (command.cooldown > 0) {
                boolean onCooldown = command.checkCooldown(event);
                if (onCooldown) {
                    return;
                }
            }
            // check for single instance
            if (command.singleInstance) {
                boolean instanceOpen = command.checkInstanceOpen(event);
                if (instanceOpen) {
                    return;
                }
            }
            if (!Users.getInstance().checkIfUserExists(event.getUser().getIdLong())) {
                try {
                    addUserToDatabase(event.getUser());
                } catch (SQLException e) {
                    logger.error("Couldn't add user to database", e);
                    return;
                }
            }
            long userId = event.getUser().getIdLong();
            command.updateCommandTrackerUser(userId);
            try {
                Users.getInstance().updateExperience(event.getUser().getIdLong(), 10, event.getUser().getAsMention(),
                        event.getChannel());
            } catch (SQLException e) {
                logger.error("Couldn't update user experience", e);
            }
            Command finalCommand = command;
            executorService.submit(() -> finalCommand.executeSlashCommand(event));
            logger.trace(String.format("Executed command: %s | Author: %s.", fullCommandName,
                    event.getUser().getName()));
        });
    }

    private void addUserToDatabase(net.dv8tion.jda.api.entities.User user) throws SQLException {
        database.model.User newUser = new database.model.User(user.getIdLong());
        userDao.add(newUser);
        Daily daily = new Daily(Objects.requireNonNull(userDao.getUserByDiscordId(user.getIdLong())).getId());
        dailyDao.add(daily);
    }
}
