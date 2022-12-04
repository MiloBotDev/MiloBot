package commands;

import commands.newcommand.NewCommand;
import commands.newcommand.ParentCommand;
import commands.newcommand.SubCommand;
import commands.newcommand.extensions.SlashCommand;
import database.dao.PrefixDao;
import database.model.Prefix;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ExecutorService;

public class NewCommandHandler extends ListenerAdapter {

    public record CommandRecord(ParentCommand command, ExecutorService executor) {
    }
    public static final Map<String, CommandRecord> commands = new HashMap<>();
    public static final Map<Long, String> prefixes = new HashMap<>();
    private final PrefixDao prefixDao = PrefixDao.getInstance();
    private final Logger logger = LoggerFactory.getLogger(CommandHandler.class);
    private final JDA jda;

    public NewCommandHandler(JDA jda) {
        this.jda = jda;
    }

    public void registerCommand(ExecutorService service, @NotNull ParentCommand command) {
        commands.put(command.getCommandName(), new CommandRecord(command, service));
    }

    public void initialize() {
        // register event listener
        jda.addEventListener(this);
        /*jda.addEventListener((EventListener) genericEvent -> commands.forEach((name, record) -> {
            record.command.listeners.forEach(listener -> record.executor.submit(() -> listener.onEvent(genericEvent)));
            record.command.subCommands.forEach(subCommand ->
                    subCommand.listeners.forEach(listener -> record.executor.submit(() -> listener.onEvent(genericEvent))));
        }));*/

        // slash commands
        ArrayList<CommandData> commandDatas = new ArrayList<>();
        for (CommandRecord commandRecord : commands.values()) {
            ParentCommand command = commandRecord.command;
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
            prefix = getGuildPrefix(event.getGuild().getIdLong());
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
            ParentCommand parentCommand = commandRecord.command;
            NewCommand command = commandRecord.command;

            if (messageParts.size() > 0) {
                for (SubCommand subCommand : parentCommand.getSubCommands()) {
                    if (subCommand.getCommandName().equals(messageParts.get(0).toLowerCase())) {
                        messageParts.remove(0);
                        command = subCommand;
                        break;
                    }
                }
            }

            try {
                command.onCommand(event, messageParts);
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
        String commandName = event.getName();
        CommandRecord commandRecord = commands.get(commandName);
        if (commandRecord == null) {
            return;
        }

        ExecutorService executorService = commandRecord.executor;
        executorService.submit(() -> {
            ParentCommand parentCommand = commandRecord.command;
            NewCommand command = commandRecord.command;

            if (event.getSubcommandName() != null) {
                for (SubCommand subCommand : parentCommand.getSubCommands()) {
                    if (subCommand.getCommandName().equals(event.getSubcommandName())) {
                        command = subCommand;
                        break;
                    }
                }
            }

            try {
                command.onCommand(event);
            } catch (Exception e) {
                logger.error("Error while slash executing command " + command.getFullCommandName(), e);
            }
        });
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        setGuildPrefix(event.getGuild().getIdLong(), null);
    }

    public boolean setGuildPrefix(long guildId, String prefix) {
        try (Connection con = DatabaseConnection.getConnection()) {
            if (prefix == null) {
                prefixDao.deleteByGuildId(con, guildId);
                return true;
            } else {
                con.setAutoCommit(false);
                Prefix prefixDbObj = prefixDao.getPrefixByGuildId(con, guildId, RowLockType.FOR_UPDATE);
                if (prefixDbObj != null) {
                    prefixDbObj.setPrefix(prefix);
                    prefixDao.update(con, prefixDbObj);
                } else {
                    prefixDao.add(con, new Prefix(guildId, prefix));
                }
                con.commit();
            }
        } catch (SQLException e) {
            logger.error("Could not update prefix for guild", e);
            return false;
        }
        return true;
    }

    public String getGuildPrefix(long guildId) {
        String dbPrefix;
        try (Connection con = DatabaseConnection.getConnection()) {
            Prefix prefixObj = prefixDao.getPrefixByGuildId(con, guildId, RowLockType.NONE);
            if (prefixObj == null) {
                dbPrefix = Config.getInstance().getDefaultPrefix();
            } else {
                dbPrefix = prefixObj.getPrefix();
            }
        } catch (SQLException e) {
            logger.error("Error while getting prefix from database", e);
            return null;
        }

        return dbPrefix;
    }
}
