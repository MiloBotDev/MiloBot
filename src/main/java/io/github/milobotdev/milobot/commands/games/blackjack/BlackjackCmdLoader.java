package io.github.milobotdev.milobot.commands.games.blackjack;

import io.github.milobotdev.milobot.commands.CommandHandler;
import io.github.milobotdev.milobot.commands.ButtonHandler;
import io.github.milobotdev.milobot.games.BlackjackGame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlackjackCmdLoader {

    public static void load() {
        ExecutorService blackjackExecutor = Executors.newSingleThreadExecutor();

        BlackjackCmd blackjackParentCmd = new BlackjackCmd(blackjackExecutor);
        blackjackParentCmd.addSubCommand(new BlackjackPlayCmd(blackjackExecutor));
        blackjackParentCmd.addSubCommand(new BlackjackStatsCmd(blackjackExecutor));
        blackjackParentCmd.addSubCommand(new BlackjackInfoCmd(blackjackExecutor));
        blackjackParentCmd.addSubCommand(new BlackjackLeaderboardCmd(blackjackExecutor));

        CommandHandler.getInstance().registerCommand(blackjackParentCmd);

        ButtonHandler buttonHandler = ButtonHandler.getInstance();
        buttonHandler.registerButton("hit", true, ButtonHandler.DeferType.NONE,
                blackjackExecutor, (event) -> {
            BlackjackGame game = BlackjackGame.getGameByAuthorId(event.getUser().getIdLong());
            if (game != null) {
                game.hit(event);
            }
        });

        buttonHandler.registerButton("stand", true, ButtonHandler.DeferType.NONE,
                blackjackExecutor, (event) -> {
            BlackjackGame game = BlackjackGame.getGameByAuthorId(event.getUser().getIdLong());
            if (game != null) {
                game.stand(event);
            }
        });

        buttonHandler.registerButton("replayBlackjack", true, ButtonHandler.DeferType.NONE,
                blackjackExecutor,
                BlackjackGame::replayBlackjack);

    }

}
