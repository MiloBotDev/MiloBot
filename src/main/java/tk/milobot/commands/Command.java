package tk.milobot.commands;

import tk.milobot.database.util.DatabaseConnection;
import tk.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.EventListener;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import tk.milobot.database.dao.CommandTrackerDao;
import tk.milobot.database.dao.UserDao;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Basic implementation of a command.
 */
// moving to NewCommand
@Deprecated(since="12/4/22", forRemoval=true)
public abstract class Command {

    private static final Logger logger = LoggerFactory.getLogger(Command.class);
    private final CommandTrackerDao commandTrackerDao = CommandTrackerDao.getInstance();
    private final UserDao userDao = UserDao.getInstance();

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
     * A list of all listeners this command has.
     */
    public ArrayList<EventListener> listeners = new ArrayList<>();

    /**
     * Types of channels in which this command can be used.
     */
    public Set<ChannelType> allowedChannelTypes = new HashSet<>();

    /**
     * Slash command data for parents command.
     */
    public CommandData slashCommandData;

    /**
     * Slash command data for subcommands.
     */
    public SubcommandData slashSubcommandData;

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
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.reply("This command has not yet been implemented.").queue();
    }

    /**
     * The default implementation for checking if a flag is present.
     */
    public boolean checkForFlags(MessageReceivedEvent event, @NotNull List<String> args) {
        boolean flagPresent = false;
        // checks if --help flag is present as an argument
        if (args.contains("--help")) {
            EmbedBuilder embedBuilder = generateHelp(event.getGuild(), event.getAuthor());
            event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
                    Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
            flagPresent = true;
        }
        // checks if the --stats flag is present as an argument
        if (args.contains("--stats")) {
            generateStats(event);
            flagPresent = true;
        }
        return flagPresent;
    }

    /**
     * Checks if the user is using the command again before the cooldown is over.
     */
    public boolean checkCooldown(@NotNull MessageReceivedEvent event) {
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
     * Checks if the user is using the command again before the cooldown is over.
     */
    public boolean checkCooldown(@NotNull SlashCommandEvent event) {
        OffsetDateTime currentTime = event.getTimeCreated();
        String authorId = event.getUser().getId();
        OffsetDateTime newAvailableTime = event.getTimeCreated().plusSeconds(cooldown);
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
     */
    public boolean checkInstanceOpen(@NotNull MessageReceivedEvent event) {
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
     * Checks if the user is using the command again when its only allowed to have 1 instance open.
     */
    public boolean checkInstanceOpen(@NotNull SlashCommandEvent event) {
        OffsetDateTime currentTime = event.getTimeCreated();
        String authorId = event.getUser().getId();
        OffsetDateTime newAvailableTime = event.getTimeCreated().plusSeconds(instanceTime);
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
    public void updateCommandTrackerUser(long discordId) {
        try {
            commandTrackerDao.addToCommandTracker(getFullCommandName(), discordId);
        } catch (SQLException e) {
            logger.error("Failed to update command tracker.", e);
        }
    }

    /**
     * Generates a message with the stats of that specific command.
     */
    public void generateStats(@NotNull MessageReceivedEvent event) {
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            tk.milobot.database.model.User userByDiscordId = userDao.getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE);
            int userId = Objects.requireNonNull(userByDiscordId).getId();

            int personalUsage = commandTrackerDao.getUserSpecificCommandUsage(getFullCommandName(), userId);
            int globalUsage = commandTrackerDao.getGlobalCommandUsage(getFullCommandName());

            EmbedBuilder stats = new EmbedBuilder();
            EmbedUtils.styleEmbed(stats, event.getAuthor());
            stats.setTitle(String.format("Stats for %s", getFullCommandName()));
            stats.addField("Personal Usages", String.format("You have used this command %d times.", personalUsage), false);
            stats.addField("Global Usages", String.format("This command has been used a total of %d times.", globalUsage), false);

            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessageEmbeds(stats.build()).setActionRow(
                    Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
            con.commit();
        } catch (SQLException e) {
            logger.error("Failed to generate stats", e);
        }
    }

    /**
     * Generates a standard help message for when the command is called with the --help flag.
     */
    public EmbedBuilder generateHelp(@NotNull Guild guild, @NotNull User author) {
        String prefix = GuildPrefixManager.getInstance().getPrefix(guild.getIdLong());

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, author);
        info.setTitle(commandName);
        info.setDescription(commandDescription);

        StringBuilder argumentsText = getArgumentsText(prefix);
        info.addField(
                "Usage",
                argumentsText.toString(),
                false
        );

        if (!(subCommands.size() == 0)) {
            StringBuilder subCommandsText = getSubCommandsText(prefix);
            info.addField("Sub Commands", subCommandsText.toString(), false);
        }

        if (!(aliases.length == 0)) {
            StringBuilder aliasesText = new StringBuilder();
            for (int i = 0; i < aliases.length; i++) {
                aliasesText.append('`').append(aliases[i]).append('`');
                if (!(i + 1 == aliases.length)) {
                    aliasesText.append(", ");
                }
            }
            info.addField("Aliases", aliasesText.toString(), false);
        }

        if(!(allowedChannelTypes.size() == 0)) {
            StringBuilder allowedChannelTypesText = new StringBuilder();
            for (int i = 0; i < allowedChannelTypes.size(); i++) {
                allowedChannelTypesText.append('`').append(allowedChannelTypes.toArray()[i].toString()).append('`');
                if (!(i + 1 == allowedChannelTypes.size())) {
                    allowedChannelTypesText.append(", ");
                }
            }
            info.addField("Allowed Channel Types", allowedChannelTypesText.toString(), false);
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
     */
    @NotNull
    private StringBuilder getSubCommandsText(String prefix) {
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
     */
    @NotNull
    private StringBuilder getArgumentsText(String prefix) {
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
    public void sendCommandUsage(@NotNull MessageReceivedEvent event) {
        String prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, event.getAuthor());
        info.setTitle("Missing required arguments");
        info.setDescription(getArgumentsText(prefix));

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessageEmbeds(info.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    /**
     * Generates and sends a message for when the parent command has been called without a sub command.
     */
    public void sendCommandExplanation(@NotNull Event event, String prefix) {
        EmbedBuilder embed = new EmbedBuilder();
        User author;
        if(event instanceof MessageReceivedEvent) {
            author = ((MessageReceivedEvent) event).getAuthor();
        } else if(event instanceof SlashCommandEvent) {
            author = ((SlashCommandEvent) event).getUser();
        } else {
            return;
        }
        EmbedUtils.styleEmbed(embed, author);
        embed.setTitle(commandName);
        embed.setDescription("This is the base command for all " + commandName + " related commands. Please use any of the " +
                "commands listed below.");
        embed.addField("Sub Commands", getSubCommandsText(prefix).toString(), false);
        if(event instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) event).getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                    Button.secondary(author.getId() + ":delete", "Delete")).queue();
        } else {
            ((SlashCommandEvent) event).replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

    /**
     * Calculates the required amount of arguments a command has.
     *
     * @return the required amount of arguments
     */
    public int calculateRequiredArgs() {
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
    public boolean checkRequiredPermissions(@NotNull Event event) {
        boolean hasPermission = false;
        Member member = null;
        if (event instanceof MessageReceivedEvent) {
            member = ((MessageReceivedEvent) event).getMember();
        } else if (event instanceof SlashCommandEvent) {
            member = ((SlashCommandEvent) event).getMember();
        }
        if (permissions.isEmpty()) {
            hasPermission = true;
        } else if (member != null) {
            hasPermission = true;
            for (Permission permission : permissions.values()) {
                if (!member.hasPermission(permission)) {
                    hasPermission = false;
                    break;
                }
            }
        }
        return hasPermission;
    }

    /**
     * Generates and sends a message for when a user is missing the required permissions to use the command.
     */
    public void sendMissingPermissions(@NotNull Event event, String prefix) {
        Member member = null;
        User user = null;
        if (event instanceof MessageReceivedEvent) {
            member = ((MessageReceivedEvent) event).getMember();
            user = ((MessageReceivedEvent) event).getAuthor();
        } else if (event instanceof SlashCommandEvent) {
            member = ((SlashCommandEvent) event).getMember();
            user = ((SlashCommandEvent) event).getUser();
        }
        ArrayList<String> missingPermissions = new ArrayList<>();
        if (member == null) {
            // this should never happen
            if (event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent) event).getChannel().sendMessage("Something went wrong. We're sorry about that").queue();
            } else if (event instanceof SlashCommandEvent) {
                ((SlashCommandEvent) event).reply("Something went wrong. We're sorry about that").queue();
            }
            return;
        }
        EnumSet<Permission> memberPermissions = member.getPermissions();
        permissions.forEach((s, p) -> {
            if (!(memberPermissions.contains(p))) {
                missingPermissions.add(s);
            }
        });
        EmbedBuilder embed = new EmbedBuilder();
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
        EmbedUtils.styleEmbed(embed, user);
        if (event instanceof MessageReceivedEvent) {
            ((MessageReceivedEvent) event).getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                    Button.secondary(((MessageReceivedEvent) event).getAuthor().getId() + ":delete", "Delete")).queue();
        } else {
            ((SlashCommandEvent) event).replyEmbeds(embed.build()).addActionRow(
                    Button.secondary(((SlashCommandEvent) event).getUser().getId() + ":delete", "Delete")).queue();
        }
    }

    /**
     * Checks if the command can be run in a channel type.
     *
     * @param channelType the channel type to check
     * @return true if the command can be run in the channel type, false otherwise
     */
    public boolean checkChannelAllowed(ChannelType channelType) {
        return allowedChannelTypes.contains(channelType);
    }

    public void sendInvalidChannel(@NotNull Event event) {
        if (event instanceof MessageReceivedEvent) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(String.format("Invalid channel type for: %s", commandName));
            // allowed channels esparate by spaces
            embed.setDescription("This command can only be run in the following channel types: " +
                    allowedChannelTypes.stream().map(Enum::toString).collect(Collectors.joining(", ")));
            EmbedUtils.styleEmbed(embed, ((MessageReceivedEvent) event).getAuthor());
            ((MessageReceivedEvent) event).getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                    Button.secondary(((MessageReceivedEvent) event).getAuthor().getId() + ":delete", "Delete")).queue();
        } else if (event instanceof SlashCommandEvent) {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle(String.format("Invalid channel type for: %s", commandName));
            embed.setDescription("This command can only be run in the following channel types: " +
                    allowedChannelTypes.stream().map(Enum::toString).collect(Collectors.joining(", ")));
            EmbedUtils.styleEmbed(embed, ((SlashCommandEvent) event).getUser());
            ((SlashCommandEvent) event).replyEmbeds(embed.build()).addActionRow(
                    Button.secondary(((SlashCommandEvent) event).getUser().getId() + ":delete", "Delete")).queue();
        }
    }
    public String generateMarkdown() {
        StringBuilder markdown = new StringBuilder();
        markdown.append("---\n\n");
        markdown.append(String.format("<h3 id=\"%s\">%s</h3>\n\n", commandName, commandName));
        markdown.append(commandDescription).append("\n\n");
        markdown.append("#### Usage\n\n");
        String argumentsText = getArgumentsText("!")
                .toString()
                .replace("Arguments marked with * are optional, arguments marked with ** accept multiple inputs.", "");
        markdown.append(argumentsText).append("\n\n");
        if(cooldown > 0) {
            markdown.append("#### Cooldown\n\n");
            markdown.append(String.format("%d seconds.\n\n", cooldown));
        }
        if(!permissions.isEmpty()) {
            markdown.append("#### Permissions\n\n");
            permissions.forEach((permissionName, permission) -> markdown.append(String.format("`%s`", permissionName)));
            markdown.append("\n\n");
        }
        if(!subCommands.isEmpty()) {
            markdown.append("#### Sub Commands\n");
            for (Command subCommand : subCommands) {
                markdown.append("\n`").append("!").append(String.format("%s ", commandName)).append(subCommand.commandName);
                if (!(subCommand.commandArgs.length == 0)) {
                    for (int y = 0; y < subCommand.commandArgs.length; y++) {
                        markdown.append(String.format(" {%s}", subCommand.commandArgs[y]));
                    }
                }
                markdown.append("`\n").append(subCommand.commandDescription).append("\n\n");
            }
        }
        return markdown.toString();
    }

    public String parentCommandName;

    public String getFullCommandName() {
        if (parentCommandName == null) {
            return commandName;
        } else {
            return parentCommandName + " " + commandName;
        }
    }
}