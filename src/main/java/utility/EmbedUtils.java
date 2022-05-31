package utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EmbedUtils {

    /**
     * Adds ❌ as an emoji under the message that when added by the user who issued the command removes the message.
     * If the emoji isnt clicked in under 60 seconds its no longer possible to delete the message.
     * @param event - MessageReceivedEvent
     * @param consumerName - The name of the user who issued the command
     * @return The consumer that adds the emoji.
     */
    @NotNull
    public static Consumer<Message> deleteEmbedButton(@NotNull MessageReceivedEvent event, String consumerName) {
        return (message) ->  {
            message.addReaction("❌").queue();
            ListenerAdapter listener = new ListenerAdapter() {
                @Override
                public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
                    String messageId = event.getMessageId();
                    if (Objects.requireNonNull(event.getUser()).getName().equals(consumerName) &&
                            event.getReactionEmote().getAsReactionCode().equals("❌") && message.getId().equals(messageId)) {
                        event.getChannel().deleteMessageById(messageId).queue();
                        event.getJDA().removeEventListener(this);
                    }
                }
            };
            message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener), 1, TimeUnit.MINUTES);
            message.getJDA().addEventListener(listener);
        };
    }

    public static void styleEmbed(MessageReceivedEvent event, EmbedBuilder embed) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        embed.setColor(Color.BLUE);
        embed.setAuthor(event.getAuthor().getName(), null, event.getAuthor().getAvatarUrl());
        embed.setFooter(dtf.format(LocalDateTime.now()));
    }
}
