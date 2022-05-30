package commands;

import commands.utility.HelpCommand;
import commands.utility.InviteCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Handles the incoming commands.
 * @author - Ruben Eekhof
 */
public class CommandHandler extends ListenerAdapter {

    public static String prefix = "!";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        List<String> receivedMessage = Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).collect(Collectors.toList());
        if(receivedMessage.get(0).equalsIgnoreCase(prefix + "help")) {
            receivedMessage.remove(0);
            new HelpCommand().execute(event, receivedMessage);
        } else if(receivedMessage.get(0).equalsIgnoreCase(prefix + "invite")) {
            receivedMessage.remove(0);
            new InviteCommand().execute(event, receivedMessage);
        }
    }

}
