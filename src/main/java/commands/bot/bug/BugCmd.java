package commands.bot.bug;

import commands.Command;
import commands.ParentCmd;
import commands.bot.BotCmd;

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
    }

}
