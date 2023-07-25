package io.github.milobotdev.milobot.commands.command;

import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.commands.instance.LobbyInstanceManager;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.database.dao.CommandTrackerDao;
import io.github.milobotdev.milobot.utility.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public abstract class Command implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(Command.class);
    private final LobbyInstanceManager lobbyInstanceManager = LobbyInstanceManager.getInstance();

    public final void onCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (this instanceof TextCommand textCommand) {
            if (!textCommand.getAllowedChannelTypes().contains(event.getChannelType())) {
                textCommand.sendInvalidChannelMessage(event);
            }

            if (args.size() > 0 && this instanceof Flags flags) {
                if (flags.getFlags().contains(args.get(args.size() - 1))) {
                    flags.executeFlag(event, args.get(args.size() - 1));
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

            // put instance check here
            User author = event.getAuthor();
            if (textCommand instanceof Instance) {
                if(lobbyInstanceManager.isUserInLobby(author.getIdLong())) {
                    event.getMessage().reply("You can't play another game when you are already in a lobby.").queue();
                    return;
                }
                InstanceData instanceData = ((Instance) textCommand).isInstanced();
                if (instanceData.isInstanced()) {
                    boolean inInstance = ((Instance) textCommand).manageInstance(event.getChannel(), author,
                            instanceData.gameType(), instanceData.duration());
                    if(inInstance) {
                        return;
                    }
                }
            }

            doUserCommandUpdates(author, event.getChannel());

            ((TextCommand) this).executeCommand(event, args);
        }
    }

    public final void onCommand(@NotNull SlashCommandInteractionEvent event) {
        if (this instanceof SlashCommand slashCommand) {
            if (!slashCommand.getAllowedChannelTypes().contains(event.getChannelType())) {
                slashCommand.sendInvalidChannelMessage(event);
            }

            if (this instanceof Permissions permissions) {
                if (!permissions.hasPermission(Objects.requireNonNull(event.getMember()))) {
                    permissions.sendMissingPermissions(event);
                }
            }

            User user = event.getUser();
            if (slashCommand instanceof Instance) {
                InstanceData instanceData = ((Instance) slashCommand).isInstanced();
                if (instanceData.isInstanced()) {
                    if(lobbyInstanceManager.isUserInLobby(user.getIdLong())) {
                        event.reply("You can't play another game when you are already in a lobby.").queue();
                        return;
                    }
                    boolean inInstance = ((Instance) slashCommand).manageInstance(event.getChannel(), user,
                            instanceData.gameType(), instanceData.duration());
                    if(inInstance) {
                        return;
                    }
                }
            }

            doUserCommandUpdates(user, event.getChannel());

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
