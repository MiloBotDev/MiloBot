package tk.milobot.commands.games.dnd.encounter;

import tk.milobot.commands.Command;
import tk.milobot.commands.ParentCmd;
import tk.milobot.commands.games.dnd.DndCmd;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

/**
 * Parent command for the encounter sub commands.
 */
public class EncounterCmd extends Command implements ParentCmd, DndCmd {

    public EncounterCmd() {
        this.commandName = "encounter";
        this.commandDescription = "D&D 5e encounter generator.";
        this.subCommands.add(EncounterGeneratorCmd.getInstance());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashCommandData = new CommandData(this.commandName, this.commandDescription);
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
    }
}
