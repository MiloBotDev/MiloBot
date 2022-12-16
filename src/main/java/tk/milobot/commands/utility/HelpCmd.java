package tk.milobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tk.milobot.commands.CommandHandler;
import tk.milobot.commands.GuildPrefixManager;
import tk.milobot.commands.bot.BotCmd;
import tk.milobot.commands.command.ParentCommand;
import tk.milobot.commands.command.extensions.DefaultChannelTypes;
import tk.milobot.commands.command.extensions.DefaultFlags;
import tk.milobot.commands.command.extensions.SlashCommand;
import tk.milobot.commands.command.extensions.TextCommand;
import tk.milobot.commands.games.GamesCmd;
import tk.milobot.commands.morbconomy.MorbconomyCmd;
import tk.milobot.utility.Config;
import tk.milobot.utility.EmbedUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * Shows the user an overview of every command, or detailed information on a specific command.
 * This class is a singleton.
 */
public class HelpCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, UtilityCmd {

    private final ExecutorService executorService;

    public HelpCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("help", "Shows the user an overview of every command, or detailed information on a specific command.")
                .addOptions(new OptionData(OptionType.STRING, "command", "The command to get help for.")
                        .setRequired(false));
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("*command");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        return true;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if(args.size() == 0) {
            event.getChannel().sendMessageEmbeds(generateHelpEmbed(event.getAuthor(), event.getGuild()).build()).queue();
        } else {
            sendCommandSpecificHelpEmbed(String.join(" ", args), event);
        }
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        if(event.getOption("command") != null) {
            sendCommandSpecificHelpEmbed(event.getOption("command").getAsString(), event);
        } else {
            event.replyEmbeds(generateHelpEmbed(event.getUser(), event.getGuild()).build()).queue();
        }
    }

    private void sendCommandSpecificHelpEmbed(String commandName, Event event) {
        final boolean[] foundCommand = {false};
        CommandHandler.getInstance().getCommands().forEach((command) -> {
            if (command.getFullCommandName().equalsIgnoreCase(commandName)) {
                if(event instanceof MessageReceivedEvent) {
                    ((TextCommand) command).generateHelp((MessageReceivedEvent) event);
                } else if(event instanceof SlashCommandEvent) {
                    ((SlashCommand) command).generateHelp((SlashCommandEvent) event);
                }
                foundCommand[0] = true;
            } else {
                command.getSubCommands().forEach((subCommand) -> {
                    if (subCommand.getFullCommandName().equalsIgnoreCase(commandName)) {
                        if(event instanceof MessageReceivedEvent) {
                            ((TextCommand) subCommand).generateHelp((MessageReceivedEvent) event);
                        } else if(event instanceof SlashCommandEvent) {
                            ((SlashCommand) subCommand).generateHelp((SlashCommandEvent) event);
                        }
                        foundCommand[0] = true;
                    }
                });
            }
        });
        if(!foundCommand[0]) {
            if(event instanceof MessageReceivedEvent) {
                ((MessageReceivedEvent) event).getChannel().sendMessage(String.format("Command `%s` not found.", commandName)).queue();
            } else if(event instanceof SlashCommandEvent) {
                ((SlashCommandEvent) event).reply(String.format("Command `%s` not found.", commandName)).setEphemeral(true).queue();
            }
        }
    }

    private @NotNull EmbedBuilder generateHelpEmbed(@NotNull User author, @Nullable Guild guild) {
        String prefix;
        if(guild != null) {
            prefix = GuildPrefixManager.getInstance().getPrefix(guild.getIdLong());
        } else {
            prefix = Config.getInstance().getDefaultPrefix();
        }

        StringBuilder utility = new StringBuilder();
        StringBuilder morbconomy = new StringBuilder();
        StringBuilder games = new StringBuilder();
        StringBuilder bot = new StringBuilder();

        CommandHandler.getInstance().getCommands().forEach(command -> {
            if (command instanceof UtilityCmd) {
                utility.append(String.format("**%s%s** - %s\n", prefix, command.getCommandName(), command.getCommandDescription()));
            } else if (command instanceof MorbconomyCmd) {
                morbconomy.append(String.format("**%s%s** - %s\n", prefix, command.getCommandName(), command.getCommandDescription()));
            } else if (command instanceof GamesCmd) {
                games.append(String.format("**%s%s** - %s\n", prefix, command.getCommandName(), command.getCommandDescription()));
            } else if (command instanceof BotCmd) {
                bot.append(String.format("**%s%s** - %s\n", prefix, command.getCommandName(), command.getCommandDescription()));
            }
        });

        EmbedBuilder help = new EmbedBuilder();
        EmbedUtils.styleEmbed(help, author);
        help.setTitle("Commands");
        help.addField("Utility", utility.toString(), false);
        help.addField("Morbconomy", morbconomy.toString(), false);
        help.addField("Games", games.toString(), false);
        help.addField("Bot", bot.toString(), false);
        return help;
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
