package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.commands.GuildPrefixManager;
import io.github.milobotdev.milobot.commands.command.ICommand;
import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.utility.Config;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

public interface SlashCommand extends ICommand {

    void executeCommand(SlashCommandInteractionEvent event);
    @NotNull CommonSlashCommandData getCommandData();
    default @NotNull String getCommandName() {
        return getCommandData().getName();
    }
    @NotNull String getCommandDescription();

    // this will be removed in the future, JDA 5 will support setting if a command is guild-only in the CommandData
    @NotNull Set<ChannelType> getAllowedChannelTypes();
    default void sendInvalidChannelMessage(@NotNull SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("Invalid channel type for: %s", getCommandData().getName()));
        embed.setDescription("This command can only be run in the following channel types: " +
                getAllowedChannelTypes().stream().map(Enum::toString).collect(Collectors.joining(", ")));
        EmbedUtils.styleEmbed(embed, event.getUser());
        event.replyEmbeds(embed.build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }

    default void generateHelp(@NotNull SlashCommandInteractionEvent event) {
        String prefix;
        if (event.isFromGuild()) {
            prefix = GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong());
        } else {
            prefix = Config.getInstance().getPrivateChannelPrefix();
        }

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, event.getUser());
        info.setTitle(getFullCommandName());
        info.setDescription(getCommandDescription());

        if (this instanceof SubCommand) {
            String argumentsText = getSlashCommandsArgumentsText(prefix);
            info.addField("Usage", argumentsText, false);
        }

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

        event.replyEmbeds(info.build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }

    default @NotNull String getSlashCommandsArgumentsText(@NotNull String prefix) {
        StringBuilder argumentsText = new StringBuilder();
        argumentsText.append('`').append(prefix).append(getFullCommandName());
        SlashCommandData slashCommandData = (SlashCommandData) getCommandData();
        if(slashCommandData.getOptions().size() == 0) {
            argumentsText.append('`');
            return argumentsText.toString();
        }
        slashCommandData.getOptions().forEach(optionData -> {
            optionData.getChoices().forEach(choice -> {
                argumentsText.append(" {").append(prefix).append(choice.getName()).append("} ");
            });
        });
        argumentsText.append('`');
        argumentsText.append("\n Arguments marked with * are optional, " +
                "arguments marked with ** accept multiple inputs.");
        return argumentsText.toString();
    }
}
