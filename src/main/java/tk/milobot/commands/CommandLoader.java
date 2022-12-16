package tk.milobot.commands;

import tk.milobot.commands.bot.BotCmdLoader;
import tk.milobot.commands.games.blackjack.BlackjackCmdLoader;
import tk.milobot.commands.games.dnd.encounter.EncounterCmdLoader;
import tk.milobot.commands.games.hungergames.HungerGamesCmdLoader;
import tk.milobot.commands.games.poker.PokerCmdLoader;
import tk.milobot.commands.games.uno.UnoCmdLoader;
import tk.milobot.commands.games.wordle.WordleCmdLoader;
import tk.milobot.commands.loaders.CommonLoader;
import tk.milobot.commands.morbconomy.MorbconomyCmdLoader;
import tk.milobot.commands.morbconomy.bank.BankCmdLoader;
import tk.milobot.commands.morbconomy.daily.DailyCmdLoader;
import tk.milobot.commands.utility.UtilityCmdLoader;
import tk.milobot.utility.lobby.LobbyLoader;
import tk.milobot.utility.paginator.PaginatorLoader;

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
    }
}
