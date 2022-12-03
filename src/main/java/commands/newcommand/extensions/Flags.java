package commands.newcommand.extensions;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;
import java.util.Set;

public interface Flags extends TextCommand {

    Set<String> getFlags();
    void executeFlag(MessageReceivedEvent event, String flag);
}