package commands;

import commands.games.blackjack.BlackjackCmd;
import commands.games.poker.PokerCmd;
import net.dv8tion.jda.api.JDA;

import java.util.concurrent.Executors;

public class NewCommandLoader {
    public static void loadAllCommands(JDA jda) {
        NewCommandHandler handler = new NewCommandHandler(jda);
        handler.registerCommand(Executors.newSingleThreadExecutor(), new PokerCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new BlackjackCmd());
        handler.initialize();
    }
}
