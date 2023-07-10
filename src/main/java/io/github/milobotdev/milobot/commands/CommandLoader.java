package io.github.milobotdev.milobot.commands;

import io.github.milobotdev.milobot.commands.bot.BotCmdLoader;
import io.github.milobotdev.milobot.commands.games.minesweeper.MinesweeperCmdLoader;
import io.github.milobotdev.milobot.commands.games.wordle.WordleCmdLoader;
import io.github.milobotdev.milobot.commands.loaders.CommonLoader;
import io.github.milobotdev.milobot.commands.morbconomy.daily.DailyCmdLoader;
import io.github.milobotdev.milobot.commands.utility.UtilityCmdLoader;
import io.github.milobotdev.milobot.utility.lobby.LobbyLoader;
import io.github.milobotdev.milobot.utility.paginator.PaginatorLoader;
import io.github.milobotdev.milobot.commands.games.blackjack.BlackjackCmdLoader;
import io.github.milobotdev.milobot.commands.games.dnd.encounter.EncounterCmdLoader;
import io.github.milobotdev.milobot.commands.games.hungergames.HungerGamesCmdLoader;
import io.github.milobotdev.milobot.commands.games.poker.PokerCmdLoader;
import io.github.milobotdev.milobot.commands.games.uno.UnoCmdLoader;
import io.github.milobotdev.milobot.commands.morbconomy.MorbconomyCmdLoader;
import io.github.milobotdev.milobot.commands.morbconomy.bank.BankCmdLoader;

/**
 * This class is responsible for loading all commands.
 */
public class CommandLoader {

    public static void initialize() {
        // put every command initializer call here
        // keep this method as short as possible, you should only have one-liners here, like SomeClass.load()
        // or SomeClass.getInstance().load()
        // if you need more than one line to initialize something, it means that it should be in its own class

        CommonLoader.load();
        LobbyLoader.load();
        PaginatorLoader.load();
        BlackjackCmdLoader.load();
        EncounterCmdLoader.load();
        PokerCmdLoader.load();
        WordleCmdLoader.load();
        HungerGamesCmdLoader.load();
        BotCmdLoader.load();
        BankCmdLoader.load();
        DailyCmdLoader.load();
        UnoCmdLoader.load();
        UtilityCmdLoader.load();
        MorbconomyCmdLoader.load();
        MinesweeperCmdLoader.load();
    }
}
