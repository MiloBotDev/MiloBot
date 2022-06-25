package events;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import javax.annotation.Nonnull;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * An event triggered when the bot is ready.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class OnReadyEvent extends ListenerAdapter {

    final static Logger logger = LoggerFactory.getLogger(OnReadyEvent.class);

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        Config config = Config.getInstance();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        TextChannel logs = Objects.requireNonNull(event.getJDA().getGuildById(config.testGuildId))
                .getTextChannelsByName(config.loggingChannelName, true).get(0);

        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(Color.green);
        embed.setDescription("Bot is ready.");
        embed.setFooter(dtf.format(LocalDateTime.now()));

        logs.sendTyping().queue();
        logs.sendMessageEmbeds(embed.build()).queue();
        logger.info("Bot is ready.");
    }
}
