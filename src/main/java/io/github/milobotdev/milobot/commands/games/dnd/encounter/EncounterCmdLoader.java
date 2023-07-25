package io.github.milobotdev.milobot.commands.games.dnd.encounter;

import io.github.milobotdev.milobot.commands.ButtonHandler;
import io.github.milobotdev.milobot.commands.CommandHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EncounterCmdLoader {

    public static void load() {
        ExecutorService dndExecutor = Executors.newSingleThreadExecutor();

        EncounterCmd encounterParentCmd = new EncounterCmd(dndExecutor);
        EncounterGeneratorCmd encounterGeneratorCmd = new EncounterGeneratorCmd(dndExecutor);
        encounterParentCmd.addSubCommand(encounterGeneratorCmd);
        CommandHandler.getInstance().registerCommand(encounterParentCmd);

        ButtonHandler.getInstance().registerButton("regenerate", true, ButtonHandler.DeferType.EDIT,
                dndExecutor, encounterGeneratorCmd::regenerateEncounter);
    }
}
