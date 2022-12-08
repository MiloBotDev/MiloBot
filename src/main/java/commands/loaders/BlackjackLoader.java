package commands.loaders;

import commands.ButtonHandler;
import commands.NewCommandHandler;
import commands.games.blackjack.NewBlackjackCmd;
import commands.games.blackjack.NewBlackjackPlayCmd;
import games.BlackjackGame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlackjackLoader {

    public static void load() {
        ExecutorService blackjackExecutor = Executors.newSingleThreadExecutor();
        NewCommandHandler.getInstance().registerCommand(new NewBlackjackCmd(blackjackExecutor)
                .addSubCommand(new NewBlackjackPlayCmd(blackjackExecutor)));
        ButtonHandler.getInstance().registerButton("hit", true, ButtonHandler.DeferType.NONE,
                blackjackExecutor, (event) -> {
            BlackjackGame game = BlackjackGame.getGameByAuthorId(event.getUser().getIdLong());
            if (game != null) {
                game.hit(event);
            }
        });
        ButtonHandler.getInstance().registerButton("stand", true, ButtonHandler.DeferType.NONE,
                blackjackExecutor, (event) -> {
            BlackjackGame game = BlackjackGame.getGameByAuthorId(event.getUser().getIdLong());
            if (game != null) {
                game.stand(event);
            }
        });
        ButtonHandler.getInstance().registerButton("replayBlackjack", true, ButtonHandler.DeferType.NONE,
                blackjackExecutor,
                BlackjackGame::replayBlackjack);
        // TODO: do other blackjack subcommands
    }
}
