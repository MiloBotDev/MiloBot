package events.guild;

import commands.CommandHandler;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import newdb.dao.PrefixDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import javax.annotation.Nonnull;
import java.awt.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * An event triggered when the bot leaves a guild.
 * This event posts a message in the logging channel with info of the server it left.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class OnGuildLeaveEvent extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(OnGuildLeaveEvent.class);
    private final PrefixDao prefixDao = PrefixDao.getInstance();

    @Override
    public void onGuildLeave(@Nonnull GuildLeaveEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        Config config = Config.getInstance();
        TextChannel logs = Objects.requireNonNull(event.getJDA().getGuildById(config.getTestGuildId()))
                .getTextChannelsByName(config.getLoggingChannelName(), true).get(0);

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

        try {
            prefixDao.deleteByGuildId(event.getGuild().getIdLong());
            CommandHandler.prefixes.remove(event.getGuild().getIdLong());
        } catch (SQLException e) {
            logger.error("Error deleting prefix from database on guild leave event", e);
        }
        logger.info(String.format("Bot has been removed from: %s.", event.getGuild().getName()));
    }

}
