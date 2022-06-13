package events;

import commands.CommandHandler;
import database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class OnGuildLeaveEvent extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(OnGuildLeaveEvent.class);

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        DatabaseManager manager = DatabaseManager.getInstance();

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        TextChannel logs = Objects.requireNonNull(event.getJDA().getGuildById("920316842902454343"))
                .getTextChannelsByName("logs", true).get(0);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setImage(event.getGuild().getIconUrl());
        embed.setColor(Color.red);
        embed.setTitle(String.format("Bot has been removed from: %s", event.getGuild().getName()));

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

        manager.query(manager.deleteServerPrefix, DatabaseManager.QueryTypes.UPDATE, event.getGuild().getId());
        CommandHandler.prefixes.remove(event.getGuild().getId());
        logger.info(String.format("Bot has been removed from: %s.", event.getGuild().getName()));
    }

}
