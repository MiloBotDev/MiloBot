package commands.newcommand;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface TextCommand {
    void executeCommand(@NotNull MessageReceivedEvent event);

    Set<ChannelType> getAllowableChannels();
    boolean checkChannelAllowed(@NotNull MessageReceivedEvent event);
    default void sendInvalidChannelMessage(@NotNull MessageReceivedEvent event) {
        event.getChannel().sendMessage("This command cannot be used in this channel.").queue();
    }


}
