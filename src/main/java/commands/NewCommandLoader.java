package commands;

import commands.games.blackjack.BlackjackCmd;
import commands.games.blackjack.BlackjackPlayCmd;
import commands.games.poker.PokerCmd;
import commands.morbconomy.daily.DailyCmd;
import commands.utility.PrefixCmd;
import database.dao.UserDao;
import database.util.NewDatabaseConnection;
import database.util.RowLockType;
import games.BlackjackGame;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NewCommandLoader {
    public static void loadAllCommands(JDA jda) {
        NewCommandHandler handler = new NewCommandHandler(jda);
        NewButtonHandler buttonHandler = new NewButtonHandler();
        handler.registerCommand(Executors.newSingleThreadExecutor(), new PokerCmd());

        // blackjack
        ExecutorService blackjackService = Executors.newSingleThreadExecutor();
        handler.registerCommand(blackjackService, new BlackjackCmd());
        buttonHandler.registerButton("hit", true, blackjackService, (event) -> {
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
                event.getHook().editOriginalEmbeds(newEmbed.build()).setActionRows(ActionRow.of(
                        Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                        Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                BlackjackPlayCmd.blackjackGames.remove(event.getUser().getIdLong());
            } else {
                newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), null);
                event.getHook().editOriginalEmbeds(newEmbed.build()).queue();
            }
        });
        buttonHandler.registerButton("stand", true, blackjackService, (event) -> {
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
            event.getHook().editOriginalEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(
                    Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                    Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
            BlackjackPlayCmd.blackjackGames.remove(event.getUser().getIdLong());
        });
        buttonHandler.registerButton("replayBlackjack", true, blackjackService, (event) -> {
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
                try (Connection con = NewDatabaseConnection.getConnection()) {
                    con.setAutoCommit(false);
                    database.model.User user2 = userDao.getUserByDiscordId(con, event.getUser().getIdLong(), RowLockType.FOR_UPDATE);
                    int playerWallet = Objects.requireNonNull(user2).getCurrency();
                    int newWallet = playerWallet - bet;
                    if (newWallet < 0) {
                        event.getChannel().sendMessage(String.format("You can't bet `%d` Morbcoins, you only have `%d` in your wallet.", bet, playerWallet)).queue();
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
                event.getHook().editOriginalEmbeds(embed.build()).setActionRows(ActionRow.of(
                        Button.primary(authorId + ":replayBlackjack", "Replay"),
                        Button.secondary(authorId + ":delete", "Delete")
                )).queue();
            } else {
                embed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), null);
                event.getHook().editOriginalEmbeds(embed.build()).setActionRows(ActionRow.of(
                        Button.primary(authorId + ":stand", "Stand"),
                        Button.primary(authorId + ":hit", "Hit")
                )).queue();
            }
        });

        // daily
        handler.registerCommand(Executors.newSingleThreadExecutor(), new DailyCmd());

        handler.registerCommand(Executors.newSingleThreadExecutor(), new PrefixCmd(handler));
        // FOR TEMPORARY TESTING ONLY!!!
        // TODO: remove later
        buttonHandler.registerButton("joinLobby", true,
                Executors.newSingleThreadExecutor(), evt -> System.out.println("works"));
        jda.addEventListener(buttonHandler);
        handler.initialize();
    }
}
