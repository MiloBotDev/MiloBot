package tk.milobot.commands.newcommand;

import tk.milobot.commands.newcommand.extensions.Flags;
import tk.milobot.commands.newcommand.extensions.Permissions;
import tk.milobot.commands.newcommand.extensions.SlashCommand;
import tk.milobot.commands.newcommand.extensions.TextCommand;
import tk.milobot.database.dao.CommandTrackerDao;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.Users;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public abstract class NewCommand implements INewCommand {
    private final Logger logger = LoggerFactory.getLogger(NewCommand.class);

    public final void onCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (this instanceof TextCommand textCommand) {
            if (!textCommand.getAllowedChannelTypes().contains(event.getChannelType())) {
                textCommand.sendInvalidChannelMessage(event);
            }

            if (args.size() > 0 && this instanceof Flags flags) {
                if (flags.getFlags().contains(args.get(args.size()-1))) {
                    flags.executeFlag(event, args.get(args.size()-1));
                    return;
                }
            }

            if (this instanceof Permissions permissions) {
                if (!permissions.hasPermission(Objects.requireNonNull(event.getMember()))) {
                    permissions.sendMissingPermissions(event);
                    return;
                }
            }

            if (!textCommand.checkRequiredArgs(event, args)) {
                return;
            }

            doUserCommandUpdates(event.getAuthor(), event.getChannel());

            ((TextCommand) this).executeCommand(event, args);
        }
    }

    public final void onCommand(@NotNull SlashCommandEvent event) {
        if (this instanceof SlashCommand slashCommand) {
            if (!slashCommand.getAllowedChannelTypes().contains(event.getChannelType())) {
                slashCommand.sendInvalidChannelMessage(event);
            }

            if (this instanceof Permissions permissions) {
                if (!permissions.hasPermission(Objects.requireNonNull(event.getMember()))) {
                    permissions.sendMissingPermissions(event);
                }
            }

            doUserCommandUpdates(event.getUser(), event.getChannel());

            ((SlashCommand) this).executeCommand(event);
        }
    }

    private void doUserCommandUpdates(@NotNull User author, @NotNull MessageChannel channel) {
        // check if this user exists in the database otherwise add it
        Users.getInstance().addUserIfNotExists(author.getIdLong());
        // update the tracker
        long userId = author.getIdLong();
        updateCommandTrackerUser(userId);
        try {
            Users.getInstance().updateExperience(author.getIdLong(), 10, author.getAsMention(),
                    channel);
        } catch (SQLException e) {
            logger.error("Couldn't update user experience", e);
        }
    }

    private void updateCommandTrackerUser(long discordId) {
        try {
            CommandTrackerDao.getInstance().addToCommandTracker(getFullCommandName(), discordId);
        } catch (SQLException e) {
            logger.error("Failed to update command tracker.", e);
        }
    }
}
