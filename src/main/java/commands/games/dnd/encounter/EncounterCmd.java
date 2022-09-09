package commands.games.dnd.encounter;

import commands.Command;
import commands.ParentCmd;
import commands.games.dnd.DndCmd;

/**
 * Parent command for the encounter sub commands.
 */
public class EncounterCmd extends Command implements ParentCmd, DndCmd {

    public EncounterCmd() {
        this.commandName = "encounter";
        this.commandDescription = "D&D 5e encounter generator.";

        this.subCommands.add(EncounterGeneratorCmd.getInstance());
    }
}
