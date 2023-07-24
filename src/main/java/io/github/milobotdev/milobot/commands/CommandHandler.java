package io.github.milobotdev.milobot.commands;

import io.github.milobotdev.milobot.commands.command.Command;
import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.Aliases;
import io.github.milobotdev.milobot.commands.command.extensions.EventListeners;
import io.github.milobotdev.milobot.commands.command.extensions.SlashCommand;
import io.github.milobotdev.milobot.main.JDAManager;
import io.github.milobotdev.milobot.utility.Config;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is responsible for handling the commands of the bot. This class is a singleton.
 */
public class CommandHandler {

    private static CommandHandler instance;
    private final List<ParentCommand> commands = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final List<CommandData> commandDatas = new ArrayList<>();

    private CommandHandler() {
        // register the event listeners that will handle the commands to the JDA
        JDAManager.getInstance().getJDABuilder().addEventListeners(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                handleMessageReceived(event);
            }

            @Override
            public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
                handleSlashCommand(event);
            }
        });

        JDAManager.getInstance().addJdaBuiltAction(jda -> jda.updateCommands().addCommands(commandDatas).queue());
    }

    /**
     * Returns an instance of the command handler.
     *
     * @return an instance of the command handler.
     */
    public static synchronized CommandHandler getInstance() {
        if (instance == null) {
            instance = new CommandHandler();
        }
        return instance;
    }

    /**
     * Registers a parent command and all of its subcommands to the command handler. The parent command should have
     * all its subcommands added to it before registering it to the command handler.
     *
     * @param command the command to be registered.
     */
    public void registerCommand(@NotNull ParentCommand command) {
        commands.add(command);

        // register the event listeners of the command
        if (command instanceof EventListeners listeners) {
            for (EventListener listener : listeners.getEventListeners()) {
                JDAManager.getInstance().getJDABuilder().addEventListeners(listener);
            }
        }

        // register the vent listeners of the command's subcommands
        command.getSubCommands().forEach(subCommand -> {
            if (subCommand instanceof EventListeners subListeners) {
                for (EventListener listener : subListeners.getEventListeners()) {
                    JDAManager.getInstance().getJDABuilder().addEventListeners(listener);
                }
            }
        });

        // If the command is a slash command, build its JDA CommandData. This is necessary to register it on Discord
        // as a slash command.
        if (command instanceof SlashCommand slashCommand) {
            SlashCommandData commandData;
            try {
                commandData = slashCommand.getCommandData().getSlashCommandData();
            } catch (ClassCastException e) {
                throw new ClassCastException("Slash command \"" + command.getFullCommandName() + "\" data type is not CommandData");
            }
            for (SubCommand subCommand : command.getSubCommands()) {
                if (subCommand instanceof SlashCommand subSlashCommand) {
                    SubcommandData subcommandData;
                    try {
                        subcommandData = (SubcommandData) subSlashCommand.getCommandData().getSubcommandData();
                    } catch (ClassCastException e) {
                        throw new ClassCastException("Slash command \"" + subCommand.getFullCommandName() + "\" data type is not SubCommandData");
                    }

                    commandData.addSubcommands(subcommandData);
                }
            }

            // Add the command data to the list of command datas waiting to be registered. These command datas will
            // be registered to the JDA once the JDA is built.
            commandDatas.add(commandData);
        }
    }

    /**
     * Event handler for text commands.
     *
     * @param event the event that triggered this handler.
     */
    private void handleMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getAuthor().isBot()) {
            return;
        }

        String message = event.getMessage().getContentRaw();
        String prefix;
        if (event.isFromGuild()) {
            prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());
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

        commands.stream().filter(cmd -> cmd.getCommandName().equals(messageParts.get(0)) ||
                (cmd instanceof Aliases &&
                        (((Aliases) cmd).getAliases().contains(messageParts.get(0)))))
                .findFirst().ifPresentOrElse(command -> {
            if (messageParts.size() == 1) {
                messageParts.remove(0);
                executeCommand(command, event, messageParts);
            } else {
                messageParts.remove(0);
                command.getSubCommands().stream().filter(subCommand -> subCommand.getCommandName().equals(messageParts.get(0)) ||
                        subCommand instanceof Aliases &&
                                ((Aliases) subCommand).getAliases().contains(messageParts.get(0))).findFirst().ifPresentOrElse(subCommand -> {
                    messageParts.remove(0);
                    executeCommand(subCommand, event, messageParts);
                }, () -> executeCommand(command, event, messageParts));
            }
        }, () -> logger.trace("Command not found: " + messageParts.get(0)));
    }

    /**
     * Executes a text command.
     *
     * @param command the command to be executed.
     * @param event the event that triggered this command.
     * @param args the arguments of the command.
     */
    private void executeCommand(@NotNull Command command, @NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String fullCommandName = command.getFullCommandName();
        logger.trace("Executing text command " + fullCommandName);
        command.getExecutorService().execute(() -> {
            try {
                command.onCommand(event, args);
            } catch (Exception e) {
                logger.error("Error while executing text command " + fullCommandName, e);
            }
        });
    }

    /**
     * Event handler for slash commands.
     *
     * @param event the event that triggered this handler.
     */
    private void handleSlashCommand(@NotNull SlashCommandInteractionEvent event) {
        if (event.getUser().isBot()) {
            return;
        }

        commands.stream().filter(cmd -> cmd.getCommandName().equals(event.getName())).findFirst().ifPresentOrElse(command -> {
            if (event.getSubcommandName() == null || event.getSubcommandName() == null) {
                executeCommand(command, event);
            } else {
                command.getSubCommands().stream().filter(subCommand -> subCommand.getCommandName().equals(event.getSubcommandName()))
                        .findFirst().ifPresentOrElse(subCommand -> executeCommand(subCommand, event), () -> executeCommand(command, event));
            }
        }, () -> logger.trace("Command not found: " + event.getName()));
    }

    /**
     * Executes a slash command.
     *
     * @param command the command to be executed.
     * @param event the event that triggered this command.
     */
    private void executeCommand(@NotNull Command command, @NotNull SlashCommandInteractionEvent event) {
        command.getExecutorService().execute(() -> {
            try {
                command.onCommand(event);
            } catch (Exception e) {
                logger.error("Error while executing text command " + command.getFullCommandName(), e);
            }
        });
    }

    /**
     * Returns a list of all registered commands.
     *
     * @return a list of all registered commands.
     */
    public List<ParentCommand> getCommands() {
        return commands;
    }
}
