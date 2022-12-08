package tk.milobot.commands;

import tk.milobot.commands.bot.StatusCmd;
import tk.milobot.commands.bot.bug.BugCmd;
import tk.milobot.commands.games.blackjack.BlackjackCmd;
import tk.milobot.commands.games.dnd.encounter.EncounterCmd;
import tk.milobot.commands.games.hungergames.HungerGamesCmd;
import tk.milobot.commands.games.poker.PokerCmd;
import tk.milobot.commands.games.uno.UnoCmd;
import tk.milobot.commands.games.wordle.WordleCmd;
import tk.milobot.commands.morbconomy.ProfileCmd;
import tk.milobot.commands.morbconomy.bank.BankCmd;
import tk.milobot.commands.morbconomy.daily.DailyCmd;
import commands.utility.*;
import tk.milobot.commands.utility.*;
import tk.milobot.games.BlackjackGame;
import tk.milobot.games.PokerGame;
import net.dv8tion.jda.api.JDA;
import tk.milobot.utility.Paginator;
import tk.milobot.utility.lobby.AbstractLobby;
import tk.milobot.utility.lobby.BotLobby;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// moving to NewCommandLoader
@Deprecated(since="12/4/22", forRemoval = true)
public class CommandLoader {

    public static void loadAllCommands(JDA jda) {
        CommandHandler handler = new CommandHandler(jda);
        ButtonHandler buttonHandler = ButtonHandler.getInstance();

        // blackjack
        ExecutorService blackjackService = Executors.newSingleThreadExecutor();
        handler.registerCommand(blackjackService, new BlackjackCmd());
        buttonHandler.registerButton("hit", true, ButtonHandler.DeferType.NONE, blackjackService, (event) -> {
            BlackjackGame game = BlackjackGame.getGameByAuthorId(event.getUser().getIdLong());
            if (game != null) {
                game.hit(event);
            }
        });
        buttonHandler.registerButton("stand", true, ButtonHandler.DeferType.NONE, blackjackService, (event) -> {
            BlackjackGame game = BlackjackGame.getGameByAuthorId(event.getUser().getIdLong());
            if (game != null) {
                game.stand(event);
            }
        });
        buttonHandler.registerButton("replayBlackjack", true, ButtonHandler.DeferType.NONE, blackjackService,
                BlackjackGame::replayBlackjack);

        // bot commands
        handler.registerCommand(Executors.newSingleThreadExecutor(), new BugCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new StatusCmd());
        // game commands
        handler.registerCommand(Executors.newSingleThreadExecutor(), new BlackjackCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new EncounterCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new HungerGamesCmd());

        ExecutorService pokerExecutor = Executors.newSingleThreadExecutor();
        handler.registerCommand(pokerExecutor, new PokerCmd());
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


        handler.registerCommand(Executors.newSingleThreadExecutor(), new UnoCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new WordleCmd());
        // morbconomy commands
        handler.registerCommand(Executors.newSingleThreadExecutor(), new BankCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new DailyCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new ProfileCmd());
        // utility commands
        handler.registerCommand(Executors.newSingleThreadExecutor(), new DailyCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new PrefixCmd(handler));
        handler.registerCommand(Executors.newSingleThreadExecutor(), HelpCmd.getInstance(handler));
        handler.registerCommand(Executors.newSingleThreadExecutor(), new InviteCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new ServerCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new UserCmd());


        // paginator buttons
        ExecutorService paginatorService = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("nextPage", true, ButtonHandler.DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.nextPage();
            }
        });
        buttonHandler.registerButton("previousPage", true, ButtonHandler.DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.previousPage();
            }
        });
        buttonHandler.registerButton("deletePaginator", true, ButtonHandler.DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.remove();
            }
        });

        // lobby buttons
        ExecutorService lobbyService = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("joinLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.addPlayer(event.getUser());
            }
        });
        buttonHandler.registerButton("leaveLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.removePlayer(event.getUser());
            }
        });
        buttonHandler.registerButton("fillLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                if (lobby instanceof BotLobby botLobby) {
                    botLobby.fill();
                } else {
                    throw new ClassCastException("Only a bot lobby can be filled with random bots.");
                }
            }
        });
        buttonHandler.registerButton("startLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.start();
            }
        });
        buttonHandler.registerButton("deleteLobby", false, ButtonHandler.DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.remove();
            }
        });

        // generic buttons
        ExecutorService genericButtonHandler = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("delete", false, ButtonHandler.DeferType.NONE, genericButtonHandler,
                (event) -> event.getMessage().delete().queue());

        jda.addEventListener(buttonHandler);
        handler.initialize();
    }

}
