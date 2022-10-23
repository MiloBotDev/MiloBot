package commands.utility;

import commands.Command;
import commands.CommandHandler;
import commands.bot.BotCmd;
import commands.games.GamesCmd;
import commands.games.dnd.DndCmd;
import commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shows the user an overview of every command, or detailed information on a specific command.
 * This class is a singleton.
 */
public class HelpCmd extends Command implements UtilityCmd {

    private static HelpCmd instance;
    private final CommandHandler handler;
    private EmbedBuilder help;

    private HelpCmd(CommandHandler handler) {
        this.handler = handler;
        this.commandName = "help";
        this.commandDescription = "Shows the user a list of available commands.";
        this.commandArgs = new String[]{"*command"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }

    /**
     * Return the only instance of this class or make a new one if no instance exists.
     */
    public static HelpCmd getInstance(CommandHandler handler) {
        if (instance == null) {
            instance = new HelpCmd(handler);
        }
        return instance;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String authorId = event.getAuthor().getId();
        if (args.size() > 0) {
            AtomicBoolean commandFound = new AtomicBoolean(false);
            String arg = args.get(0);
            CommandHandler.commands.forEach((key, value) -> {
                if (key.contains(arg.toLowerCase(Locale.ROOT))) {
                    EmbedBuilder embedBuilder = value.command().generateHelp(event.getGuild(), event.getAuthor());
                    event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
                            Button.secondary(authorId + ":delete", "Delete")).queue();
                    commandFound.set(true);
                }
            });
            if (!commandFound.get()) {
                event.getChannel().sendMessage(String.format("%s not found.", arg)).queue();
            }
        } else {
            createEmbed(event.getAuthor(), event.getGuild());
            EmbedUtils.styleEmbed(help, event.getAuthor());
            event.getChannel().sendMessageEmbeds(help.build()).setActionRow(Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
        }
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        String authorId = event.getUser().getId();
        if (!(event.getOption("command") == null)) {
            AtomicBoolean commandFound = new AtomicBoolean(false);
            String command = Objects.requireNonNull(event.getOption("command")).getAsString();
            CommandHandler.commands.forEach((key, value) -> {
                if (key.contains(command.toLowerCase(Locale.ROOT))) {
                    EmbedBuilder embedBuilder = value.command().generateHelp(Objects.requireNonNull(event.getGuild()), event.getUser());
                    event.replyEmbeds(embedBuilder.build()).addActionRow(Button.secondary(authorId + ":delete", "Delete")).queue();
                    commandFound.set(true);
                }
            });
            if (!commandFound.get()) {
                event.reply(String.format("%s not found.", command)).queue();
            }
        } else {
            createEmbed(event.getUser(), Objects.requireNonNull(event.getGuild()));
            EmbedUtils.styleEmbed(help, event.getUser());
            event.replyEmbeds(help.build()).addActionRow(Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
        }
    }

    /**
     * Builds the embeds for the help command.
     */
    private void createEmbed(@NotNull User author, @NotNull Guild guild) {
        String prefix = handler.prefixes.get(guild.getIdLong());

        StringBuilder utility = new StringBuilder();
        StringBuilder morbconomy = new StringBuilder();
        StringBuilder games = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        StringBuilder dnd = new StringBuilder();

        handler.commands.forEach((key, value) -> {
            Command command = value.command();
            if (command instanceof UtilityCmd) {
                utility.append(String.format("**%s%s** - %s\n", prefix, command.commandName, command.commandDescription));
            } else if (command instanceof MorbconomyCmd) {
                morbconomy.append(String.format("**%s%s** - %s\n", prefix, command.commandName, command.commandDescription));
            } else if (command instanceof GamesCmd) {
                games.append(String.format("**%s%s** - %s\n", prefix, command.commandName, command.commandDescription));
            } else if (command instanceof BotCmd) {
                bot.append(String.format("**%s%s** - %s\n", prefix, command.commandName, command.commandDescription));
            } else if (command instanceof DndCmd) {
                dnd.append(String.format("**%s%s** - %s\n", prefix, command.commandName, command.commandDescription));
            }
        });

        this.help = new EmbedBuilder();
        help.setTitle("Commands");
        help.addField("Utility", utility.toString(), false);
        help.addField("Morbconomy", morbconomy.toString(), false);
        help.addField("Games", games.toString(), false);
        help.addField("Bot", bot.toString(), false);
        help.addField("Dungeons & Dragons", dnd.toString(), false);
    }

}
