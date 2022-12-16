package tk.milobot.commands;

import net.dv8tion.jda.api.hooks.EventListener;
import tk.milobot.commands.command.Command;
import tk.milobot.commands.command.ParentCommand;
import tk.milobot.commands.command.SubCommand;
import tk.milobot.commands.command.extensions.Aliases;
import tk.milobot.commands.command.extensions.EventListeners;
import tk.milobot.commands.command.extensions.Instance;
import tk.milobot.commands.command.extensions.SlashCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.main.JDAManager;
import tk.milobot.utility.Config;
import tk.milobot.utility.TimeTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandHandler {

    private static CommandHandler instance;
    private final List<ParentCommand> commands = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final List<CommandData> commandDatas = new ArrayList<>();

    private CommandHandler() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                handleMessageReceived(event);
            }

            @Override
            public void onSlashCommand(@NotNull SlashCommandEvent event) {
                handleSlashCommand(event);
            }
        });

        JDAManager.getInstance().addJdaBuiltAction(jda -> jda.updateCommands().addCommands(commandDatas).queue());
    }

    public static synchronized CommandHandler getInstance() {
        if (instance == null) {
            instance = new CommandHandler();
        }
        return instance;
    }

    public void registerCommand(@NotNull ParentCommand command) {
        commands.add(command);
        if (command instanceof EventListeners listeners) {
            for (EventListener listener : listeners.getEventListeners()) {
                JDAManager.getInstance().getJDABuilder().addEventListeners(listener);
            }
        }
        command.getSubCommands().forEach(subCommand -> {
            if (subCommand instanceof EventListeners subListeners) {
                for (EventListener listener : subListeners.getEventListeners()) {
                    JDAManager.getInstance().getJDABuilder().addEventListeners(listener);
                }
            }
        });
        if (command instanceof SlashCommand slashCommand) {
            CommandData commandData;
            try {
                commandData = (CommandData) slashCommand.getCommandData();
            } catch (ClassCastException e) {
                throw new ClassCastException("Slash command \"" + command.getFullCommandName() + "\" data type is not CommandData");
            }
            for (SubCommand subCommand : command.getSubCommands()) {
                if (subCommand instanceof SlashCommand subSlashCommand) {
                    SubcommandData subcommandData;
                    try {
                        subcommandData = (SubcommandData) subSlashCommand.getCommandData();
                    } catch (ClassCastException e) {
                        throw new ClassCastException("Slash command \"" + subCommand.getFullCommandName() + "\" data type is not SubCommandData");
                    }
                    commandData.addSubcommands(subcommandData);
                }
            }
            commandDatas.add(commandData);
        }
    }

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

    private void executeCommand(@NotNull Command command, @NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String fullCommandName = command.getFullCommandName();
        logger.trace("Executing text command " + fullCommandName);
        command.getExecutorService().execute(() -> {
            try {
                if(command instanceof Instance) {
                    Map<Boolean, Integer> instanceData = ((Instance) command).isInstanced();
                    if (instanceData.containsKey(true)) {
                        GameInstanceManager gameInstanceManager = GameInstanceManager.getInstance();
                        long userId = event.getAuthor().getIdLong();
                        if(gameInstanceManager.containsUser(userId, fullCommandName)) {
                            TimeTracker userTimeTracker = gameInstanceManager.getUserTimeTracker(userId, fullCommandName);
                            event.getChannel().sendMessage(String.format("You are still in game. Please wait %d more seconds.",
                                    userTimeTracker.timeSecondsTillDuration())).queue();
                            return;
                        } else {
                            gameInstanceManager.addUser(userId, fullCommandName, instanceData.get(true));
                        }
                    }
                }
                command.onCommand(event, args);
            } catch (Exception e) {
                logger.error("Error while executing text command " + fullCommandName, e);
            }
        });
    }

    private void handleSlashCommand(@NotNull SlashCommandEvent event) {
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

    private void executeCommand(@NotNull Command command, @NotNull SlashCommandEvent event) {
        String fullCommandName = command.getFullCommandName();
        command.getExecutorService().execute(() -> {
            try {
                if(command instanceof Instance) {
                    Map<Boolean, Integer> instanceData = ((Instance) command).isInstanced();
                    if (instanceData.containsKey(true)) {
                        GameInstanceManager gameInstanceManager = GameInstanceManager.getInstance();
                        long userId = event.getUser().getIdLong();
                        if(gameInstanceManager.containsUser(userId, fullCommandName)) {
                            TimeTracker userTimeTracker = gameInstanceManager.getUserTimeTracker(userId, fullCommandName);
                            event.getChannel().sendMessage(String.format("You are still in game. Please wait %d more seconds.",
                                    userTimeTracker.timeSecondsTillDuration())).queue();
                            return;
                        } else {
                            gameInstanceManager.addUser(userId, fullCommandName, instanceData.get(true));
                        }
                    }
                }
                command.onCommand(event);
            } catch (Exception e) {
                logger.error("Error while executing text command " + command.getFullCommandName(), e);
            }
        });
    }

    public List<ParentCommand> getCommands() {
        return commands;
    }
}
