package io.github.milobotdev.milobot.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class EmbedUtils {

    /**
     * Adds ⏹ as an emoji under the message that when added by the user who issued the command removes the message.
     * If the emoji isnt clicked in under 60 seconds its no longer possible to delete the message.
     *
     * @return The consumer that adds the emoji.
     */
    @NotNull
    @Deprecated(since = "4-7-2022, migrating to using buttons instead.")
    public static Consumer<Message> deleteEmbedButton(@NotNull MessageReceivedEvent event, String consumerId) {
        return (message) -> {
            message.addReaction(Emoji.fromUnicode("⏹")).queue();
            ListenerAdapter listener = new ListenerAdapter() {
                @Override
                public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
                    String messageId = event.getMessageId();
                    if (Objects.requireNonNull(event.getUser()).getId().equals(consumerId) &&
                            event.getReaction().getEmoji().getAsReactionCode().equals("⏹") && message.getId().equals(messageId)) {
                        event.getChannel().deleteMessageById(messageId).queue();
                        event.getJDA().removeEventListener(this);
                    }
                }
            };
            message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener), 1, TimeUnit.MINUTES);
            message.getJDA().addEventListener(listener);
        };
    }

    /**
     * Applies some default styling to an embed.
     */
    public static void styleEmbed(@NotNull EmbedBuilder embed, @NotNull User user) {
        embed.setColor(Color.BLUE);
        String avatarUrl = user.getAvatarUrl();
        embed.setAuthor(user.getName(), null, avatarUrl);
        embed.setTimestamp(new Date().toInstant());
        embed.setFooter(user.getName(), avatarUrl);
    }


    /**
     * Adds a simple paginator to the specified message.
     */
    @Deprecated(since = "4-7-2022, migrating to using buttons instead.")
    public static void createPaginator(@NotNull MessageReceivedEvent event, String title, @NotNull ArrayList<EmbedBuilder> pages,
                                       @NotNull Message message, String consumerId) {
        message.clearReactions().queue();
        EmbedBuilder embedBuilder = pages.get(0);
        message.editMessageEmbeds(embedBuilder.build()).queue(message1 -> {
                    final int[] currentPage = {0};
                    if (pages.size() > 1) {
                        message.addReaction(Emoji.fromUnicode("⏮")).queue();
                        message.addReaction(Emoji.fromUnicode("◀")).queue();
                        message.addReaction(Emoji.fromUnicode("⏹ ")).queue();
                        message.addReaction(Emoji.fromUnicode("▶")).queue();
                        message.addReaction(Emoji.fromUnicode("⏭")).queue();
                    } else {
                        message.addReaction(Emoji.fromUnicode("⏹")).queue();
                    }
                    ListenerAdapter totalGames = new ListenerAdapter() {
                        @Override
                        public void onMessageReactionAdd(@NotNull MessageReactionAddEvent eventReaction2) {
                            if (Objects.requireNonNull(eventReaction2.getUser()).getId().equals(consumerId)
                                    && message.getId().equals(message1.getId())) {
                                String asReactionCode = eventReaction2.getReaction().getEmoji().getAsReactionCode();
                                EmbedBuilder newEmbed = new EmbedBuilder();
                                newEmbed.setTitle(title);
                                EmbedUtils.styleEmbed(newEmbed, event.getAuthor());
                                switch (asReactionCode) {
                                    case "⏮":
                                        message.removeReaction(Emoji.fromUnicode(asReactionCode), eventReaction2.getUser()).queue();
                                        currentPage[0] = 0;
                                        newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
                                        message.editMessageEmbeds(newEmbed.build()).queue();
                                    case "◀":
                                        message.removeReaction(Emoji.fromUnicode(asReactionCode), eventReaction2.getUser()).queue();
                                        if (!(currentPage[0] - 1 < 0)) {
                                            currentPage[0]--;
                                            newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
                                            message.editMessageEmbeds(newEmbed.build()).queue();
                                        }
                                        break;
                                    case "⏹":
                                        event.getJDA().removeEventListener(this);
                                        event.getChannel().deleteMessageById(message.getId()).queue();
                                        break;
                                    case "▶":
                                        message.removeReaction(Emoji.fromUnicode(asReactionCode), eventReaction2.getUser()).queue();
                                        if (!(currentPage[0] + 1 == pages.size())) {
                                            currentPage[0]++;
                                            newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
                                            message.editMessageEmbeds(newEmbed.build()).queue();
                                        }
                                        break;
                                    case "⏭":
                                        message.removeReaction(Emoji.fromUnicode(asReactionCode), eventReaction2.getUser()).queue();
                                        currentPage[0] = pages.size() - 1;
                                        newEmbed.setDescription(pages.get(currentPage[0]).getDescriptionBuilder());
                                        message.editMessageEmbeds(newEmbed.build()).queue();
                                }
                            }
                        }
                    };
                    message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(totalGames),
                            2, TimeUnit.MINUTES);
                    message.getJDA().addEventListener(totalGames);
                }
        );
    }
}
