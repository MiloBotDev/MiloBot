package commands.newcommand;

import commands.newcommand.extensions.SlashCommand;
import commands.newcommand.extensions.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentCommand extends NewCommand implements IParentCommand {
    private List<SubCommand> subCommands = new ArrayList<>();

    public CommandData getCommandData() {
        throw new UnsupportedOperationException("This command does not have slash commands.");
    }

    public void addSubCommand(SubCommand subCommand) {
        subCommand.assignParentCommand(this);
        this.subCommands.add(subCommand);
    }

    public List<SubCommand> getSubCommands() {
        return new ArrayList<>(subCommands);
    }

    public String getSubCommandsText(String prefix) {
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
        return subCommandsText.toString();
    }
}
