package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class OnGuildJoinEvent extends ListenerAdapter {

    /**
     * Triggered when the bot joins a new discord server (guild).
     * @param event - GuildJoinEvent
     */
    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        TextChannel logs = Objects.requireNonNull(event.getJDA().getGuildById("920316842902454343"))
                .getTextChannelsByName("logs", true).get(0);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setImage(event.getGuild().getIconUrl());
        embed.setColor(Color.green);
        embed.setTitle(String.format("Bot has been added to: %s", event.getGuild().getName()));

        String description = event.getGuild().getDescription();
        description = description == null ? "None" : description;
        embed.addField("Server Description", description, true);

        embed.addField("Members", String.valueOf(event.getGuild().getMemberCount()), true);
        embed.addField("Channel Count", String.valueOf(event.getGuild().getChannels().size()), true);
        embed.addField("Role Count", String.valueOf(event.getGuild().getRoles().size()), true);
        embed.addField("Server Boosts", String.valueOf(event.getGuild().getBoostCount()), true);
        embed.addField("Date Created", String.valueOf(event.getGuild().getTimeCreated()), true);
        embed.setFooter(dtf.format(LocalDateTime.now()));

        logs.sendTyping().queue();
        logs.sendMessageEmbeds(embed.build()).queue();
    }

}
