package events;

import commands.games.dnd.encounter.EncounterGeneratorCmd;
import commands.games.blackjack.BlackjackPlayCmd;
import database.dao.BlackjackDao;
import database.util.NewDatabaseConnection;
import database.util.RowLockType;
import games.Blackjack;
import games.Poker;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import database.dao.UserDao;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Paginator;
import utility.Users;
import utility.lobby.AbstractLobby;
import utility.lobby.BotLobby;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

/**
 * Triggers when a button is clicked by a user.
 */
public class OnButtonClick extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(OnButtonClick.class);
    private final EncounterGeneratorCmd encCmd;
    private final UserDao userDao = UserDao.getInstance();
    private final Users userUtil;

    public OnButtonClick() {
        this.userUtil = Users.getInstance();
        this.encCmd = EncounterGeneratorCmd.getInstance();
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] id = event.getComponentId().split(":");
        String authorId = id[0];
        String type = id[1];
        User user = event.getUser();
        // Check if the user is in the database
        userUtil.addUserIfNotExists(event.getUser().getIdLong());
        if ((type.equals("joinLobby") || type.equals("leaveLobby") || type.equals("fillLobby"))) {
            event.deferEdit().queue();
            switch (type) {
                case "joinLobby" -> {
                    AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
                    if (lobby != null) {
                        lobby.addPlayer(user);
                    }
                }
                case "leaveLobby" -> {
                    AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
                    if (lobby != null) {
                        lobby.removePlayer(user);
                    }
                }
                case "fillLobby" -> {
                    AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
                    if (lobby != null) {
                        if (lobby instanceof BotLobby botLobby) {
                            botLobby.fill();
                        } else {
                            throw new ClassCastException("Only a bot lobby can be filled with random bots.");
                        }
                    }
                }
            }
            return;
        } else if (!authorId.equals(user.getId())) {
            return;
        }
        event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail

        MessageChannel channel = event.getChannel();
        switch (type) {
            case "delete":
                event.getHook().deleteOriginal().queue();
                break;
            case "nextPage":
                Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
                if (paginator != null) {
                    paginator.nextPage();
                }
                break;
            case "previousPage":
                Paginator paginator2 = Paginator.getPaginatorByMessage(event.getMessage());
                if (paginator2 != null) {
                    paginator2.previousPage();
                }
                break;
            case "regenerate":
                MessageEmbed build = encCmd.regenerateEncounter(event.getMessage().getEmbeds().get(0), event.getUser());
                event.getHook().editOriginalEmbeds(build).setActionRows(
                        ActionRow.of(Button.primary(event.getUser().getId() + ":regenerate", "Regenerate"),
                                Button.primary(event.getUser().getId() + ":save", "Save"),
                                Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                break;
            case "hit":
                Blackjack game = BlackjackPlayCmd.blackjackGames.get(user.getIdLong());
                if (game.isFinished() || game.isPlayerStand()) {
                    break;
                }
                game.playerHit();
                Blackjack.BlackjackStates blackjackStates = game.checkWin(false);
                EmbedBuilder newEmbed;
                if (blackjackStates.equals(Blackjack.BlackjackStates.DEALER_WIN)) {
                    game.checkWin(true);
                    newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                    event.getHook().editOriginalEmbeds(newEmbed.build()).setActionRows(ActionRow.of(
                            Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                            Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                    BlackjackPlayCmd.blackjackGames.remove(user.getIdLong());
                } else {
                    newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), null);
                    event.getHook().editOriginalEmbeds(newEmbed.build()).queue();
                }
                break;
            case "stand":
                Blackjack blackjack = BlackjackPlayCmd.blackjackGames.get(user.getIdLong());
                if (blackjack.isFinished() || blackjack.isPlayerStand()) {
                    break;
                }
                blackjack.setPlayerStand(true);
                blackjack.dealerMoves();
                blackjack.setDealerStand(true);
                blackjackStates = blackjack.checkWin(true);
                EmbedBuilder embedBuilder = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                event.getHook().editOriginalEmbeds(embedBuilder.build()).setActionRows(ActionRow.of(
                        Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                        Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                BlackjackPlayCmd.blackjackGames.remove(user.getIdLong());
                break;
            case "replayBlackjack":
                if (BlackjackPlayCmd.blackjackGames.containsKey(user.getIdLong())) {
                    break;
                }
                String description = event.getMessage().getEmbeds().get(0).getDescription();
                Blackjack value;
                if (description == null) {
                    value = new Blackjack(user.getIdLong());
                } else {
                    String s = description.replaceAll("[^0-9]", "");
                    int bet = Integer.parseInt(s);
                    try (Connection con = NewDatabaseConnection.getConnection()) {
                        con.setAutoCommit(false);
                        database.model.User user2 = userDao.getUserByDiscordId(con, user.getIdLong(), RowLockType.FOR_UPDATE);
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
                        value = new Blackjack(user.getIdLong(), bet);
                    } catch (SQLException e) {
                        logger.error("Error updating blackjack data when user wanted to replay blackjack.", e);
                        return;
                    }
                }
                value.initializeGame();
                BlackjackPlayCmd.blackjackGames.put(user.getIdLong(), value);
                Blackjack.BlackjackStates state = value.checkWin(false);
                EmbedBuilder embed;
                if (state.equals(Blackjack.BlackjackStates.PLAYER_BLACKJACK)) {
                    value.dealerHit();
                    value.setDealerStand(true);
                    blackjackStates = value.checkWin(true);
                    embed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                    BlackjackPlayCmd.blackjackGames.remove(user.getIdLong());
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
                break;
            case "poker_check":
                Poker pokerGame1 = Poker.getUserGame(user);
                pokerGame1.setPlayerAction(Poker.PlayerAction.CHECK);
                break;
            case "poker_call":
                Poker pokerGame2 = Poker.getUserGame(user);
                pokerGame2.setPlayerAction(Poker.PlayerAction.CALL);
                break;
            case "poker_fold":
                Poker pokerGame3 = Poker.getUserGame(user);
                pokerGame3.setPlayerAction(Poker.PlayerAction.FOLD);
                break;
            case "poker_raise":
                Poker pokerGame4 = Poker.getUserGame(user);
                pokerGame4.setPlayerAction(Poker.PlayerAction.RAISE);
                break;
            case "startLobby":
                AbstractLobby lobby = AbstractLobby.getLobbyByMessage(event.getMessage());
                if (lobby != null) {
                    lobby.start();
                }
                break;
            case "deleteLobby":
                AbstractLobby lobby4 = AbstractLobby.getLobbyByMessage(event.getMessage());
                if (lobby4 != null) {
                    lobby4.remove();
                }
                break;
            case "deletePaginator":
                Paginator paginator4 = Paginator.getPaginatorByMessage(event.getMessage());
                if (paginator4 != null) {
                    paginator4.remove();
                }
                break;
        }

    }
}
