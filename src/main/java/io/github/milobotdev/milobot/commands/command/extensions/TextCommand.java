package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.database.dao.CommandTrackerDao;
import io.github.milobotdev.milobot.database.dao.UserDao;
import io.github.milobotdev.milobot.database.model.User;
import io.github.milobotdev.milobot.commands.GuildPrefixManager;
import io.github.milobotdev.milobot.commands.command.ICommand;
import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.database.util.DatabaseConnection;
import io.github.milobotdev.milobot.database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import io.github.milobotdev.milobot.utility.Config;
import io.github.milobotdev.milobot.utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface TextCommand extends ICommand {

    /**
     * This method is fired when a command is executed.
     * Override this method and implement the command logic.
     * @param event the event that's triggered when the command is called.
     * @param args a list of the arguments that have been passed by the user when the command was called.
     */
    void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args);

    /**
     * Returns a list of all the arguments this command has.
     * Override this method to add arguments to a command.
     * If the command doesn't have any arguments implement the DefaultCommandChannels interface.
     *
     * @return a list of all the command arguments.
     * @see DefaultCommandArgs
     */
    List<String> getCommandArgs();

    /**
     * Checks if all the required arguments are given when the command is called.
     * Override this method and perform checks on the arguments if necessary.
     * If the command doesn't have any arguments implement the DefaultCommandChannels interface.
     *
     * @param event the event that's triggered when the command is called.
     * @param args a list of the arguments that have been passed by the user when the command was called.
     * @see DefaultCommandArgs
     */
    boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args);

    /**
     * Returns a set with the channel types this command can be used in.
     * Overrides this method to add the allowed channel types.
     * If the command can be used in every channel implement the DefaultChannelTypes interface.
     *
     * @return a set with the channel types this command can be used in.
     * @see ChannelType
     * @see DefaultChannelTypes
     */
    Set<ChannelType> getAllowedChannelTypes();

    /**
     * Generates and sends a standardized help message for the command this is called on.
     *
     * @param event the event that's triggered when the command is called.
     */
    default void generateHelp(@NotNull MessageReceivedEvent event) {
        String prefix;
        if (event.isFromGuild()) {
            prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());
        } else {
            prefix = Config.getInstance().getPrivateChannelPrefix();
        }

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, event.getAuthor());
        info.setTitle(getFullCommandName());
        info.setDescription(getCommandDescription());

        String argumentsText = getTextCommandsArgumentsText(prefix);
        info.addField("Usage", argumentsText, false);

        if (this instanceof ParentCommand parentCommand && parentCommand.getSubCommands().size() > 0) {
            String subCommandsText = parentCommand.getSubCommandsText(prefix);
            info.addField("Sub Commands", subCommandsText, false);
        }

        if (this instanceof Aliases) {
            String aliasesText = ((Aliases) this).getAliases().stream().map(s -> "`" + s + "`")
                    .collect(Collectors.joining(", "));
            info.addField("Aliases", aliasesText, false);
        }

        Set<ChannelType> allowedChannelTypes = getAllowedChannelTypes();
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

        if (this instanceof Flags flags) {
            Set<String> flagsSet = flags.getFlags();
            StringBuilder flagsText = new StringBuilder();
            Iterator<String> iter = flagsSet.iterator();
            while (iter.hasNext()) {
                flagsText.append('`').append(iter.next()).append('`');
                if (iter.hasNext()) {
                    flagsText.append(", ");
                }
            }
            info.addField("Flags", flagsText.toString(), false);
        }

        // TODO implement new cooldown system
        /*if (cooldown > 0) {
            info.addField("Cooldown", String.format("%d seconds.", cooldown), false);
        }*/

        if (this instanceof Permissions permissions) {
            info.addField("Permissions", permissions.getPermissionsText(), false);
        }

        event.getChannel().sendMessageEmbeds(info.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    default void generateStats(@NotNull MessageReceivedEvent event) {
        try(Connection con = DatabaseConnection.getConnection()) {
            con.setAutoCommit(false);
            User userByDiscordId = UserDao.getInstance().getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE);
            int userId = Objects.requireNonNull(userByDiscordId).getId();

            int personalUsage = CommandTrackerDao.getInstance().getUserSpecificCommandUsage(getFullCommandName(), userId);
            int globalUsage = CommandTrackerDao.getInstance().getGlobalCommandUsage(getFullCommandName());

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
            LoggerFactory.getLogger(TextCommand.class).error("Failed to generate stats", e);
        }
    }

    default @NotNull String getTextCommandsArgumentsText(@NotNull String prefix) {
        StringBuilder argumentsText = new StringBuilder();
        List<String> commandArgs = getCommandArgs();
        if (commandArgs.size() == 0) {
            argumentsText.append("`").append(prefix).append(getFullCommandName()).append("`");
        } else {
            argumentsText.append("`").append(prefix).append(getFullCommandName()).append(" ");
            for (int i = 0; i < commandArgs.size(); i++) {
                argumentsText.append("{").append(commandArgs.get(i)).append("}");
                if (!(i + 1 == commandArgs.size())) {
                    argumentsText.append(" ");
                }
            }
            argumentsText.append('`');
            argumentsText.append("\n Arguments marked with * are optional, " +
                    "arguments marked with ** accept multiple inputs.");
        }
        return argumentsText.toString();
    }

    default void sendMissingArgs(@NotNull MessageReceivedEvent event) {
        String prefix;
        if (event.isFromGuild()) {
            prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());
        } else {
            prefix = Config.getInstance().getPrivateChannelPrefix();
        }

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, event.getAuthor());
        info.setTitle("Missing required arguments");
        info.setDescription(getTextCommandsArgumentsText(prefix));

        event.getChannel().sendMessageEmbeds(info.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    default void sendTooManyArgs(@NotNull MessageReceivedEvent event) {
        String prefix;
        if (event.isFromGuild()) {
            prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());
        } else {
            prefix = Config.getInstance().getPrivateChannelPrefix();
        }

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, event.getAuthor());
        info.setTitle("Too many arguments");
        info.setDescription(getTextCommandsArgumentsText(prefix));

        event.getChannel().sendMessageEmbeds(info.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    default void sendInvalidArgs(@NotNull MessageReceivedEvent event, @NotNull String message) {
        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, event.getAuthor());
        info.setTitle("Arguments error");
        info.setDescription(message);

        event.getChannel().sendMessageEmbeds(info.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    default void sendInvalidChannelMessage(@NotNull MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("Invalid channel type for: %s", this.getCommandName()));
        // allowed channels esparate by spaces
        embed.setDescription("This command can only be run in the following channel types: " +
                getAllowedChannelTypes().stream().map(Enum::toString).collect(Collectors.joining(", ")));
        EmbedUtils.styleEmbed(embed, event.getAuthor());
        event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }
}
