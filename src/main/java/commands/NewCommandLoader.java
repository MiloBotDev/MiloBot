package commands;

import commands.games.blackjack.NewBlackjackCmd;
import commands.games.blackjack.NewBlackjackPlayCmd;
import net.dv8tion.jda.api.JDA;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewCommandLoader {

    public static void loadAllCommands(JDA jda) {
        NewCommandHandler handler = new NewCommandHandler(jda);

        ExecutorService blackjackServiice = Executors.newSingleThreadExecutor();
        handler.registerCommand(new NewBlackjackCmd(blackjackServiice)
                .addSubCommand(new NewBlackjackPlayCmd(blackjackServiice)));

        handler.initialize();
    }
}
