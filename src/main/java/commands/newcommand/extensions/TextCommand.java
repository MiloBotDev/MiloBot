package commands.newcommand.extensions;

import commands.GuildPrefixManager;
import commands.newcommand.INewCommand;
import commands.newcommand.ParentCommand;
import database.dao.CommandTrackerDao;
import database.dao.UserDao;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.LoggerFactory;
import utility.Config;
import utility.EmbedUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public interface TextCommand extends INewCommand {

    void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args);
    @NotNull List<String> getCommandArgs();
    boolean checkRequiredArgs(@NotNull MessageReceivedEvent event, @NotNull List<String> args);
    @NotNull Set<ChannelType> getAllowedChannelTypes();

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

        String argumentsText = getArgumentsText(prefix);
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
            database.model.User userByDiscordId = UserDao.getInstance().getUserByDiscordId(con, event.getAuthor().getIdLong(), RowLockType.FOR_UPDATE);
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

    default @NotNull String getArgumentsText(@NotNull String prefix) {
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
        info.setDescription(getArgumentsText(prefix));

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
        info.setDescription(getArgumentsText(prefix));

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
