package commands.dnd.encounter;

import commands.Command;
import commands.SubCmd;
import database.DatabaseManager;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Lets users load their saved encounters from the database.
 * This class is a singleton.
 */
public class EncounterLoadCmd extends Command implements SubCmd {

	private final DatabaseManager manager;
	private static EncounterLoadCmd instance;

	private EncounterLoadCmd() {
		this.commandName = "load";
		this.commandDescription = "Load a saved encounter.";
		this.commandArgs = new String[]{"id*"};
		this.manager = DatabaseManager.getInstance();
	}

	public static EncounterLoadCmd getInstance() {
		if(instance == null) {
			instance = new EncounterLoadCmd();
		}
		return instance;
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandEvent event) {
	}


}
