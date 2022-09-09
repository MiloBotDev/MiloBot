package commands.utility;

import commands.Command;
import commands.CommandLoader;
import database.dao.CommandTrackerDao;
import database.dao.UserDao;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utility.EmbedUtils;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The Usage command.
 * Shows the user statistics on how much each command has been used.
 */
public class UsageCmd extends Command implements UtilityCmd {

    private final CommandTrackerDao commandTrackerDao = CommandTrackerDao.getInstance();
    private final UserDao userDao = UserDao.getInstance();

    public UsageCmd() {
        this.commandName = "usage";
        this.commandDescription = "See the amount of times each command has been used.";
        this.cooldown = 60;
        this.commandArgs = new String[]{"*command"};
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if(args.size() == 0) {
            event.getChannel().sendMessageEmbeds(createUsageEmbed(event.getAuthor(), null).build()).queue();
        } else {
            event.getChannel().sendMessageEmbeds(createUsageEmbed(event.getAuthor(), String.join(" ", args)).build()).queue();
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.getHook().sendMessageEmbeds(createUsageEmbed(event.getUser(), null).build()).queue();
    }

    private @NotNull EmbedBuilder createUsageEmbed(@NotNull User user, @Nullable String optionalCommand) {
        EmbedBuilder usageEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(usageEmbed, user);
        AtomicReference<StringBuilder> sb = new AtomicReference<>(new StringBuilder());
        AtomicBoolean commandFound = new AtomicBoolean(false);
        try {
            int id = userDao.getUserByDiscordId(user.getIdLong()).getId();
            CommandLoader.commandList.forEach((commandNames, command) -> commandNames.forEach(commandName -> {
                try {
                    if (Arrays.stream(command.aliases).noneMatch(s -> Objects.equals(s, commandName))) {
                        if(commandName.equals(optionalCommand)) {
                            sb.set(new StringBuilder().append(String.format("**%s:** %d times.\n", commandName,
                                    commandTrackerDao.getGlobalCommandUsage(commandName))));
                            commandFound.set(true);
                        }
                        if (!commandFound.get()) {
                            sb.get().append(String.format("**%s:** %d times.\n", commandName,
                                    commandTrackerDao.getGlobalCommandUsage(commandName)));
                        }
                        if (command.subCommands.size() > 0) {
                            command.subCommands.forEach(command1 -> {
                                try {
                                    String subCommandName = String.format("%s %s", commandName, command1.commandName);
                                    if(subCommandName.equals(optionalCommand)) {
                                        sb.set(new StringBuilder().append(String.format("**%s:** %d times.\n", subCommandName,
                                                commandTrackerDao.getGlobalCommandUsage(subCommandName))));
                                        commandFound.set(true);
                                        return;
                                    }
                                    if (!commandFound.get()) {
                                        sb.get().append(String.format("- **%s:** %d times.\n", subCommandName,
                                                commandTrackerDao.getGlobalCommandUsage(subCommandName)));
                                    }
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            });
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if(!commandFound.get() && optionalCommand != null) {
            return usageEmbed
                    .setTitle("Command not found.")
                    .setDescription(String.format("`%s` is an invalid command.", optionalCommand));
        }
        usageEmbed.setTitle("Command Usage");
        usageEmbed.setDescription(sb.get().toString());
        return usageEmbed;
    }

}
