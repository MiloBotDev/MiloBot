package commands.utility;

import commands.Command;
import commands.CommandLoader;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.List;
import java.util.Locale;

/**
 * The Help command.
 * Shows the user an overview of every command, or detailed information on a specific command.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class HelpCommand extends Command implements UtilityCommand{

    public HelpCommand() {
        this.commandName = "help";
        this.commandDescription = "Shows the user a list of available commands.";
        this.commandArgs = new String[]{"*command"};
        this.aliases = new String[]{"info"};
    }

    @Override
    public void execute(@NotNull MessageReceivedEvent event, List<String> args) {
        if(checkForFlags(event, args, commandName, commandDescription, commandArgs, aliases, flags, cooldown)) {return;}
        if(args.size() > 0) {
            CommandLoader.commandList.forEach((key, value) -> {
                if (key.contains(args.get(0).toLowerCase(Locale.ROOT))) {
                    value.generateHelp(event, value.commandName, value.commandDescription, value.commandArgs,
                            value.aliases, value.flags, value.cooldown);
                }
            });
        } else {
            String consumerName = event.getAuthor().getName();
            EmbedBuilder utilityEmbed = new EmbedBuilder();
            EmbedUtils.styleEmbed(event, utilityEmbed);
            utilityEmbed.setTitle("Utility Commands");
            CommandLoader.commandList.forEach((key, value) -> {
                if(value instanceof UtilityCommand) {
                    utilityEmbed.addField(value.commandName, value.commandDescription, true);
                }
            });

            event.getChannel().sendTyping().queue();
            event.getChannel().sendMessageEmbeds(utilityEmbed.build()).queue(EmbedUtils.deleteEmbedButton(event, consumerName));
        }
    }

}
