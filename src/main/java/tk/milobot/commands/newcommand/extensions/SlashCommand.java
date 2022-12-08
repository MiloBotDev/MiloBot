package tk.milobot.commands.newcommand.extensions;

import tk.milobot.commands.newcommand.INewCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import tk.milobot.utility.EmbedUtils;

import java.util.Set;
import java.util.stream.Collectors;

public interface SlashCommand extends INewCommand {

    void executeCommand(SlashCommandEvent event);
    @NotNull BaseCommand<?> getCommandData();
    default @NotNull String getCommandName() {
        return getCommandData().getName();
    }
    default @NotNull String getCommandDescription() {
        return getCommandData().getDescription();
    }

    // this will be removed in the future, JDA 5 will support setting if a command is guild-only in the CommandData
    @NotNull Set<ChannelType> getAllowedChannelTypes();
    default void sendInvalidChannelMessage(@NotNull SlashCommandEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle(String.format("Invalid channel type for: %s", getCommandData().getName()));
        embed.setDescription("This command can only be run in the following channel types: " +
                getAllowedChannelTypes().stream().map(Enum::toString).collect(Collectors.joining(", ")));
        EmbedUtils.styleEmbed(embed, event.getUser());
        event.replyEmbeds(embed.build()).addActionRow(
                Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
    }
}