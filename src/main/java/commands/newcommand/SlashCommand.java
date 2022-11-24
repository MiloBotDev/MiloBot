package commands.newcommand;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface SlashCommand {

    void executeCommand(@NotNull SlashCommandEvent event);
    BaseCommand<?> getCommandData();

    // this will be removed in the future, JDA 5 will support setting if a command is guild-only in the CommandData
    Set<ChannelType> getAllowableChannels();
    boolean checkChannelAllowed(@NotNull MessageReceivedEvent event);
    default void sendInvalidChannelMessage(@NotNull MessageReceivedEvent event) {
        event.getChannel().sendMessage("This command cannot be used in this channel.").queue();
    }
}
