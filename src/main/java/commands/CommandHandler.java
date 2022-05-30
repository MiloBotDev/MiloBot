package commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Handles the incoming commands.
 * @author - Ruben Eekhof
 */
public class CommandHandler extends ListenerAdapter {

    public String prefix = "!";

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        List<String> receivedMessage = Arrays.stream(event.getMessage().getContentRaw().split("\\s+"))
                .map(String::toLowerCase).collect(Collectors.toList());
        if(receivedMessage.get(0).startsWith(prefix)) {
            CommandLoader.commandList.keySet().stream().takeWhile(i -> receivedMessage.size() > 0).forEach(strings -> {
                if(strings.contains(receivedMessage.get(0).toLowerCase(Locale.ROOT).replace(prefix, ""))) {
                    receivedMessage.remove(0);
                    CommandLoader.commandList.get(strings).execute(event, receivedMessage);
                }
            });
        }
    }

}
