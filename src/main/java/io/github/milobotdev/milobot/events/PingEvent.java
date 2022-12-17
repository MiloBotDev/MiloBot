package io.github.milobotdev.milobot.events;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import io.github.milobotdev.milobot.commands.GuildPrefixManager;
import io.github.milobotdev.milobot.utility.Config;

public class PingEvent extends ListenerAdapter {

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getMessage().getMentionedUsers().contains(event.getJDA().getSelfUser())) {
            if (event.getChannelType().equals(ChannelType.TEXT)) {
                event.getChannel().sendMessage("My prefix is `" + GuildPrefixManager.getInstance()
                        .getPrefix(event.getGuild().getIdLong()) + "`").queue();
            } else if (event.getChannelType().equals(ChannelType.PRIVATE)) {
                event.getChannel().sendMessage("My prefix is `" + Config.getInstance().getPrivateChannelPrefix() +
                        "`").queue();
            }
        }
    }
}
