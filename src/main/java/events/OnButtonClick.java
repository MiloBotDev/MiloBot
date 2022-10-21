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
