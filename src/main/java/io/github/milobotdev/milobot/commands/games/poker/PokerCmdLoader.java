package io.github.milobotdev.milobot.commands.games.poker;

import io.github.milobotdev.milobot.commands.ButtonHandler;
import io.github.milobotdev.milobot.commands.CommandHandler;
import io.github.milobotdev.milobot.games.PokerGame;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PokerCmdLoader {

    public static void load() {
        ExecutorService pokerExecutor = Executors.newSingleThreadExecutor();

        PokerCmd pokerParentCmd = new PokerCmd(pokerExecutor);
        pokerParentCmd.addSubCommand(new PokerPlayCmd(pokerExecutor));
        CommandHandler.getInstance().registerCommand(pokerParentCmd);

        ButtonHandler buttonHandler = ButtonHandler.getInstance();
        buttonHandler.registerButton("poker_check", true, ButtonHandler.DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.CHECK);
            }
        });
        buttonHandler.registerButton("poker_call", true, ButtonHandler.DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.CALL);
            }
        });
        buttonHandler.registerButton("poker_fold", true, ButtonHandler.DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.FOLD);
            }
        });
        buttonHandler.registerButton("poker_raise", true, ButtonHandler.DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.RAISE);
            }
        });
    }
}
