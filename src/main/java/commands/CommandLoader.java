package commands;

import commands.ButtonHandler.DeferType;
import commands.bot.StatusCmd;
import commands.bot.bug.BugCmd;
import commands.games.blackjack.BlackjackCmd;
import commands.games.blackjack.BlackjackPlayCmd;
import commands.games.dnd.encounter.EncounterCmd;
import commands.games.hungergames.HungerGamesCmd;
import commands.games.poker.PokerCmd;
import commands.games.uno.UnoCmd;
import commands.games.wordle.WordleCmd;
import commands.morbconomy.ProfileCmd;
import commands.morbconomy.bank.BankCmd;
import commands.morbconomy.daily.DailyCmd;
import commands.utility.*;
import database.dao.UserDao;
import database.util.DatabaseConnection;
import database.util.RowLockType;
import games.BlackjackGame;
import games.PokerGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Paginator;
import utility.lobby.AbstractLobby;
import utility.lobby.BotLobby;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandLoader {

    public static void loadAllCommands(JDA jda) {
        CommandHandler handler = new CommandHandler(jda);
        ButtonHandler buttonHandler = new ButtonHandler();
        handler.registerCommand(Executors.newSingleThreadExecutor(), new PokerCmd());

        // blackjack
        ExecutorService blackjackService = Executors.newSingleThreadExecutor();
        handler.registerCommand(blackjackService, new BlackjackCmd());
        buttonHandler.registerButton("hit", true, DeferType.NONE, blackjackService, (event) -> {
            BlackjackGame game = BlackjackPlayCmd.blackjackGames.get(event.getUser().getIdLong());
            if (game.isFinished() || game.isPlayerStand()) {
                return;
            }
            game.playerHit();
            BlackjackGame.BlackjackStates blackjackStates = game.checkWin(false);
            EmbedBuilder newEmbed;
            if (blackjackStates.equals(BlackjackGame.BlackjackStates.DEALER_WIN)) {
                game.checkWin(true);
                newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                event.editMessageEmbeds(newEmbed.build()).setActionRows(ActionRow.of(
                        Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                        Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                BlackjackPlayCmd.blackjackGames.remove(event.getUser().getIdLong());
            } else {
                newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), null);
                event.editMessageEmbeds(newEmbed.build()).queue();
            }
        });
        buttonHandler.registerButton("stand", true, DeferType.NONE, blackjackService, (event) -> {
            BlackjackGame.BlackjackStates blackjackStates;
            BlackjackGame blackjackGame = BlackjackPlayCmd.blackjackGames.get(event.getUser().getIdLong());
            if (blackjackGame.isFinished() || blackjackGame.isPlayerStand()) {
                return;
            }
            blackjackGame.setPlayerStand(true);
            blackjackGame.dealerMoves();
            blackjackGame.setDealerStand(true);
            blackjackStates = blackjackGame.checkWin(true);
            EmbedBuilder embedBuilder = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
            event.editMessageEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(
                    Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                    Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
            BlackjackPlayCmd.blackjackGames.remove(event.getUser().getIdLong());
        });
        buttonHandler.registerButton("replayBlackjack", true, DeferType.NONE, blackjackService, (event) -> {
            Logger logger = LoggerFactory.getLogger(BlackjackGame.class);
            UserDao userDao = UserDao.getInstance();
            BlackjackGame.BlackjackStates blackjackStates;
            String authorId = event.getUser().getId();
            if (BlackjackPlayCmd.blackjackGames.containsKey(event.getUser().getIdLong())) {
                return;
            }
            String description = event.getMessage().getEmbeds().get(0).getDescription();
            BlackjackGame value;
            if (description == null) {
                value = new BlackjackGame(event.getUser().getIdLong());
            } else {
                String s = description.replaceAll("[^0-9]", "");
                int bet = Integer.parseInt(s);
                try (Connection con = DatabaseConnection.getConnection()) {
                    con.setAutoCommit(false);
                    database.model.User user2 = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
                    int playerWallet = Objects.requireNonNull(user2).getCurrency();
                    int newWallet = playerWallet - bet;
                    if (newWallet < 0) {
                        event.reply(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
                        con.commit();
                        return;
                    }
                    user2.setCurrency(newWallet);
                    userDao.update(con, user2);
                    con.commit();
                    value = new BlackjackGame(event.getUser().getIdLong(), bet);
                } catch (SQLException e) {
                    logger.error("Error updating blackjack data when user wanted to replay blackjack.", e);
                    return;
                }
            }
            value.initializeGame();
            BlackjackPlayCmd.blackjackGames.put(event.getUser().getIdLong(), value);
            BlackjackGame.BlackjackStates state = value.checkWin(false);
            EmbedBuilder embed;
            if (state.equals(BlackjackGame.BlackjackStates.PLAYER_BLACKJACK)) {
                value.dealerHit();
                value.setDealerStand(true);
                blackjackStates = value.checkWin(true);
                embed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                BlackjackPlayCmd.blackjackGames.remove(event.getUser().getIdLong());
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                        Button.primary(authorId + ":replayBlackjack", "Replay"),
                        Button.secondary(authorId + ":delete", "Delete")
                )).queue();
            } else {
                embed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), null);
                event.editMessageEmbeds(embed.build()).setActionRows(ActionRow.of(
                        Button.primary(authorId + ":stand", "Stand"),
                        Button.primary(authorId + ":hit", "Hit")
                )).queue();
            }
        });

        // bot commands
        handler.registerCommand(Executors.newSingleThreadExecutor(), new BugCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new StatusCmd());
        // game commands
        handler.registerCommand(Executors.newSingleThreadExecutor(), new BlackjackCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new EncounterCmd());
        handler.registerCommand(Executors.newSingleThreadExecutor(), new HungerGamesCmd());

        ExecutorService pokerExecutor = Executors.newSingleThreadExecutor();
        handler.registerCommand(pokerExecutor, new PokerCmd());
        buttonHandler.registerButton("poker_check", true, DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.CHECK);
            }
        });
        buttonHandler.registerButton("poker_call", true, DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.CALL);
            }
        });
        buttonHandler.registerButton("poker_fold", true, DeferType.EDIT, pokerExecutor, (event) -> {
            PokerGame pokerGame = PokerGame.getUserGame(event.getUser());
            if (pokerGame != null) {
                pokerGame.setPlayerAction(PokerGame.PlayerAction.FOLD);
            }
        });
        buttonHandler.registerButton("poker_raise", true, DeferType.EDIT, pokerExecutor, (event) -> {
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
        buttonHandler.registerButton("nextPage", true, DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.nextPage();
            }
        });
        buttonHandler.registerButton("previousPage", true, DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.previousPage();
            }
        });
        buttonHandler.registerButton("deletePaginator", true, DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.remove();
            }
        });

        // lobby buttons
        ExecutorService lobbyService = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("joinLobby", false, DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.addPlayer(event.getUser());
            }
        });
        buttonHandler.registerButton("leaveLobby", false, DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.removePlayer(event.getUser());
            }
        });
        buttonHandler.registerButton("fillLobby", false, DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                if (lobby instanceof BotLobby botLobby) {
                    botLobby.fill();
                } else {
                    throw new ClassCastException("Only a bot lobby can be filled with random bots.");
                }
            }
        });
        buttonHandler.registerButton("startLobby", false, DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.start();
            }
        });
        buttonHandler.registerButton("deleteLobby", false, DeferType.EDIT, lobbyService, (event) -> {
            AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
            if (lobby != null) {
                lobby.remove();
            }
        });


        // generic buttons
        ExecutorService genericButtonHandler = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("delete", false, DeferType.NONE, genericButtonHandler, (event) -> {
            event.getMessage().delete().queue();
        });

        jda.addEventListener(buttonHandler);
        handler.initialize();
    }

}
