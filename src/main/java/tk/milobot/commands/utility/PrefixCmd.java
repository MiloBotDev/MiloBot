package tk.milobot.commands.utility;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import tk.milobot.commands.GuildPrefixManager;
import tk.milobot.commands.command.ParentCommand;
import tk.milobot.commands.command.extensions.DefaultChannelTypes;
import tk.milobot.commands.command.extensions.DefaultFlags;
import tk.milobot.commands.command.extensions.SlashCommand;
import tk.milobot.commands.command.extensions.TextCommand;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Change the prefix the bot listens to for a guild.
 */
public class PrefixCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, UtilityCmd {

    private final ExecutorService executorService;

    public PrefixCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("prefix", "Change the prefix of the guild you're in.")
                .addOptions(new OptionData(OptionType.STRING, "prefix", "The new prefix for the guild.")
                        .setRequired(true));
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("*prefix");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        if(args.size() == 0) {
            event.getChannel().sendMessage("You must provide a prefix.").queue();
            return false;
        }
        String prefix = args.get(0);
        if (prefix.length() > 2) {
            event.getChannel().sendMessage("A prefix cant be longer then 2 characters.").queue();
            return false;
        }
        if (!isValidPrefix(prefix)) {
            event.getChannel().sendMessage(String.format("`%s` is not a valid prefix", prefix)).queue();
            return false;
        }
        return true;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String prefix = args.get(0);
        GuildPrefixManager.getInstance().setPrefix(event.getGuild().getIdLong(), prefix);
        event.getChannel().sendMessage(String.format("Prefix changed to `%s`", prefix)).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        String prefix = Objects.requireNonNull(event.getOption("prefix")).getAsString();
        if (prefix.length() > 2) {
            event.reply("A prefix cant be longer then 2 characters.").queue();
        } else {
            if (isValidPrefix(prefix)) {
                long id = Objects.requireNonNull(event.getGuild()).getIdLong();
                updatePrefix(prefix, id);
                event.reply(String.format("Prefix successfully changed to: %s", prefix)).queue();
            } else {
                event.reply(String.format("`%s` is not a valid prefix", prefix)).queue();
            }
        }
    }

    private void updatePrefix(String prefix, long id) {
        GuildPrefixManager.getInstance().setPrefix(id, prefix);
    }

    private boolean isValidPrefix(@NotNull String prefix) {
        return !prefix.toLowerCase(Locale.ROOT).contains("*");
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
