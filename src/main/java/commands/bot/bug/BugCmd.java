package commands.bot.bug;

import commands.Command;
import commands.ParentCmd;
import commands.bot.BotCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Parent command for all Bug sub commands.
 */
public class BugCmd extends Command implements BotCmd, ParentCmd {

    private final static ResourceBundle resourceBundle = ResourceBundle.getBundle("localization.MiloBot_en_US", Locale.getDefault());

    public BugCmd() {
        this.commandName = resourceBundle.getString("bugCommandName");
        this.commandDescription = resourceBundle.getString("bugCommandDescription");
        this.subCommands.add(new BugReportCmd());
        this.subCommands.add(new BugViewCmd());
        this.subCommands.add(new BugListCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
    }

}
