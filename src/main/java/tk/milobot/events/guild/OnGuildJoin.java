package tk.milobot.events.guild;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import tk.milobot.database.dao.PrefixDao;
import tk.milobot.database.model.Prefix;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.Config;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * An event triggered when the bot joins a new guild.
 */
public class OnGuildJoin extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(OnGuildJoin.class);
    private final PrefixDao prefixDao = PrefixDao.getInstance();

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        Config config = Config.getInstance();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        TextChannel logs = Objects.requireNonNull(event.getJDA().getGuildById(config.getTestGuildId()))
                .getTextChannelsByName(config.getLoggingChannelName(), true).get(0);

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

        Prefix prefix = new Prefix(event.getGuild().getIdLong(), config.getDefaultPrefix());
        /*try {
            prefixDao.add(prefix);
            CommandHandler.prefixes.put(event.getGuild().getIdLong(), config.getDefaultPrefix());
        } catch (SQLException e) {
            logger.error("Error adding prefix on guild join event", e);
        }*/
        logger.trace(String.format("Bot has been added to: %s.", event.getGuild().getName()));
    }

}
