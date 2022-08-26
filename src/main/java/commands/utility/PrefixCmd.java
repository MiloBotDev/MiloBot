package commands.utility;

import commands.Command;
import commands.CommandHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import newdb.dao.PrefixDao;
import newdb.model.Prefix;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Change the prefix the bot listens to for a guild.
 */
public class PrefixCmd extends Command implements UtilityCmd {
    private final Logger logger = LoggerFactory.getLogger(PrefixCmd.class);
    private final PrefixDao prefixDao = PrefixDao.getInstance();

    public PrefixCmd() {
        this.commandName = "prefix";
        this.commandDescription = "Change the prefix of the guild you're in.";
        this.commandArgs = new String[]{"prefix"};
        this.cooldown = 60;
        this.permissions.put("Administrator", Permission.ADMINISTRATOR);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String prefix = args.get(0);
        if (prefix.length() > 2) {
            event.getChannel().sendMessage("A prefix cant be longer then 2 characters.").queue();
        } else {
            if (isValidPrefix(prefix)) {
                long id = event.getGuild().getIdLong();
                if (updatePrefix(prefix, id)) {
                    event.getChannel().sendMessage(String.format("Prefix successfully changed to: %s", prefix)).queue();
                }
            } else {
                event.getChannel().sendMessage(String.format("`%s` is not a valid prefix", prefix)).queue();
            }
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        String prefix = Objects.requireNonNull(event.getOption("prefix")).getAsString();
        if (prefix.length() > 2) {
            event.reply("A prefix cant be longer then 2 characters.").queue();
        } else {
            if (isValidPrefix(prefix)) {
                long id = Objects.requireNonNull(event.getGuild()).getIdLong();
                if (updatePrefix(prefix, id)) {
                    event.reply(String.format("Prefix successfully changed to: %s", prefix)).queue();
                }
            } else {
                event.reply(String.format("`%s` is not a valid prefix", prefix)).queue();
            }
        }
    }

    private boolean updatePrefix(String prefix, long id) {
        Prefix prefixDbObj;
        try {
            prefixDbObj = prefixDao.getPrefixByGuildId(id);
        } catch (SQLException e) {
            logger.error("Could not get prefix for guild", e);
            return false;
        }
        Objects.requireNonNull(prefixDbObj).setPrefix(prefix);
        try {
            prefixDao.update(prefixDbObj);
        } catch (SQLException e) {
            logger.error("Could not update prefix for guild", e);
            return false;
        }
        CommandHandler.prefixes.replace(id, prefix);
        return true;
    }

    private boolean isValidPrefix(@NotNull String prefix) {
        return !prefix.toLowerCase(Locale.ROOT).contains("*");
    }

}
