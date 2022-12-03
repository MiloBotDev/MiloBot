package commands.newcommand;

import commands.newcommand.extensions.SlashCommand;
import commands.newcommand.extensions.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.Set;
import java.util.stream.Collectors;

public interface INewCommand {

    String getFullCommandName();
    Set<ChannelType> getAllowedChannelTypes();
    default void sendInvalidChannelMessage(@NotNull MessageReceivedEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        if (this instanceof TextCommand) {
            embed.setTitle(String.format("Invalid channel type for: %s", ((TextCommand) this).getCommandName()));
        } else if (this instanceof SlashCommand) {
            embed.setTitle(String.format("Invalid channel type for: %s", ((SlashCommand) this).getCommandData().getName()));
        } else {
            embed.setTitle("Invalid channel type for command.");
        }
        // allowed channels esparate by spaces
        embed.setDescription("This command can only be run in the following channel types: " +
                getAllowedChannelTypes().stream().map(Enum::toString).collect(Collectors.joining(", ")));
        EmbedUtils.styleEmbed(embed, event.getAuthor());
        event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }
}
