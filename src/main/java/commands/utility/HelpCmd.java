package commands.utility;

import commands.Command;
import commands.CommandHandler;
import commands.CommandLoader;
import commands.bot.BotCmd;
import commands.games.dnd.DndCmd;
import commands.games.GamesCmd;
import commands.morbconomy.MorbconomyCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
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

    private ArrayList<List<ActionRow>> buttons;
    private EmbedBuilder help;

    private HelpCmd() {
        this.commandName = "help";
        this.commandDescription = "Shows the user a list of available commands.";
        this.commandArgs = new String[]{"*command"};
    }

    /**
     * Return the only instance of this class or make a new one if no instance exists.
     */
    public static HelpCmd getInstance() {
        if (instance == null) {
            instance = new HelpCmd();
        }
        return instance;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        String authorId = event.getAuthor().getId();
        if (args.size() > 0) {
            AtomicBoolean commandFound = new AtomicBoolean(false);
            String arg = args.get(0);
            CommandLoader.commandList.forEach((key, value) -> {
                if (key.contains(arg.toLowerCase(Locale.ROOT))) {
                    EmbedBuilder embedBuilder = value.generateHelp(event.getGuild(), event.getAuthor());
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
            CommandLoader.commandList.forEach((key, value) -> {
                if (key.contains(command.toLowerCase(Locale.ROOT))) {
                    EmbedBuilder embedBuilder = value.generateHelp(Objects.requireNonNull(event.getGuild()), event.getUser());
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
        String prefix = CommandHandler.prefixes.get(guild.getIdLong());

        StringBuilder utility = new StringBuilder();
        StringBuilder morbconomy = new StringBuilder();
        StringBuilder games = new StringBuilder();
        StringBuilder bot = new StringBuilder();
        StringBuilder dnd = new StringBuilder();

        CommandLoader.commandList.forEach((key, value) -> {
            if (value instanceof UtilityCmd) {
                utility.append(String.format("**%s%s** - %s\n", prefix, value.commandName, value.commandDescription));
            } else if (value instanceof MorbconomyCmd) {
                morbconomy.append(String.format("**%s%s** - %s\n", prefix, value.commandName, value.commandDescription));
            } else if (value instanceof GamesCmd) {
                games.append(String.format("**%s%s** - %s\n", prefix, value.commandName, value.commandDescription));
            } else if (value instanceof BotCmd) {
                bot.append(String.format("**%s%s** - %s\n", prefix, value.commandName, value.commandDescription));
            } else if (value instanceof DndCmd) {
                dnd.append(String.format("**%s%s** - %s\n", prefix, value.commandName, value.commandDescription));
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
