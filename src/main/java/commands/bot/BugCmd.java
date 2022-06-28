package commands.bot;

import commands.Command;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BugCmd extends Command implements BotCmd {

	public BugCmd() {
		this.commandName = "bug";
		this.commandDescription = "Add bugs to the bots issue tracker, or view them.";
		this.subCommands.add(new BugReportCmd());
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		event.getChannel().sendMessage("This is the parent command").queue();
	}

}
