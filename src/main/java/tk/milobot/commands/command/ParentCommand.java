package tk.milobot.commands.command;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.Nullable;
import tk.milobot.commands.GuildPrefixManager;
import tk.milobot.commands.command.extensions.*;
import org.jetbrains.annotations.NotNull;
import tk.milobot.utility.Config;
import tk.milobot.utility.EmbedUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class ParentCommand extends Command implements IParentCommand {
    private final List<SubCommand> subCommands = new ArrayList<>();

    @Override
    public @NotNull ParentCommand addSubCommand(@NotNull SubCommand subCommand) {
        subCommand.assignParentCommand(this);
        this.subCommands.add(subCommand);
        return this;
    }

    @Override
    public @NotNull List<SubCommand> getSubCommands() {
        return new ArrayList<>(subCommands);
    }

    @Override
    public @NotNull String getSubCommandsText(@NotNull String prefix) {
        StringBuilder subCommandsText = new StringBuilder();
        for (SubCommand subCommand : getSubCommands()) {
            subCommandsText.append("\n`").append(prefix).append(subCommand.getFullCommandName());
            if (subCommand instanceof TextCommand tSubCommand) {
                List<String> commandArgs = tSubCommand.getCommandArgs();
                for (String commandArg : commandArgs) {
                    subCommandsText.append(String.format(" {%s}", commandArg));
                }
                subCommandsText.append("`\n").append(tSubCommand.getCommandDescription());
            } else if (subCommand instanceof SlashCommand sSubCommand) {
                subCommandsText.append("`\n").append(sSubCommand.getCommandData().getDescription());
            } else {
                throw new IllegalStateException("Sub command is not a text or slash command.");
            }

        }
        if(subCommandsText.isEmpty()) {
            subCommandsText.append("No sub commands available.");
        }
        return subCommandsText.toString();
    }

}
