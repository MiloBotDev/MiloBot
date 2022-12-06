package commands;

import commands.newcommand.NewCommand;
import commands.newcommand.ParentCommand;
import commands.newcommand.SubCommand;
import commands.newcommand.extensions.Aliases;
import commands.newcommand.extensions.EventListeners;
import commands.newcommand.extensions.SlashCommand;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NewCommandHandler extends ListenerAdapter {

    private final List<ParentCommand> commands = new ArrayList<>();
    private final Logger logger = LoggerFactory.getLogger(NewCommandHandler.class);
    private final JDA jda;

    public NewCommandHandler(JDA jda) {
        this.jda = jda;
    }

    public void registerCommand(@NotNull ParentCommand command) {
        commands.add(command);
    }

    public void initialize() {
        // register event listener
        jda.addEventListener(this);

        commands.forEach(command -> {
            if (command instanceof EventListeners listeners) {
                jda.addEventListener(listeners);
            }
            command.getSubCommands().forEach(subCommand -> {
                if (subCommand instanceof EventListeners subListeners) {
                    jda.addEventListener(subListeners);
                }
            });
        });

        // slash commands
        ArrayList<CommandData> commandDatas = new ArrayList<>();
        for (ParentCommand command : commands) {
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
        jda.updateCommands().addCommands(commandDatas).queue();
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
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

    private void executeCommand(NewCommand command, MessageReceivedEvent event, List<String> args) {
        logger.trace("Executing text command " + command.getFullCommandName());
        command.getExecutorService().execute(() -> {
            try {
                command.onCommand(event, args);
            } catch (Exception e) {
                logger.error("Error while executing text command " + command.getFullCommandName(), e);
            }
        });
    }

    @Override
    public void onSlashCommand(SlashCommandEvent event) {
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

    private void executeCommand(NewCommand command, SlashCommandEvent event) {
        command.getExecutorService().execute(() -> {
            try {
                command.onCommand(event);
            } catch (Exception e) {
                logger.error("Error while executing text command " + command.getFullCommandName(), e);
            }
        });
    }
}
