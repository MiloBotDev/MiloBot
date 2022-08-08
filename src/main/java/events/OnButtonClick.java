package events;

import commands.dnd.encounter.EncounterGeneratorCmd;
import commands.games.blackjack.BlackjackPlayCmd;
import commands.games.hungergames.HungerGamesStartCmd;
import database.DatabaseManager;
import database.queries.UsersTableQueries;
import games.Blackjack;
import games.HungerGames;
import games.Poker;
import models.BlackjackStates;
import models.LobbyEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.ButtonClickEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.Lobby;
import utility.Paginator;

import java.awt.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Triggers when a button is clicked by a user.
 */
public class OnButtonClick extends ListenerAdapter {

    private final EncounterGeneratorCmd encCmd;
    private final DatabaseManager dbManager;

    public OnButtonClick() {
        this.encCmd = EncounterGeneratorCmd.getInstance();
        this.dbManager = DatabaseManager.getInstance();
    }

    @Override
    public void onButtonClick(@NotNull ButtonClickEvent event) {
        String[] id = event.getComponentId().split(":");
        String authorId = id[0];
        String type = id[1];
        // Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
        User user = event.getUser();
        if (!authorId.equals(user.getId()) && (type.equals("joinLobby") || type.equals("leaveLobby"))) {
            event.deferEdit().queue();
            switch (type) {
                case "joinLobby" -> {
                    Lobby lobby = Lobby.lobbyInstances.get(event.getMessage().getId());
                    if (lobby != null) {
                        List<LobbyEntry> lobbyEntries = lobby.getPlayers();
                        for (LobbyEntry lobbyEntry : lobbyEntries) {
                            if (lobbyEntry.userId().equals(user.getId())) {
                                event.getHook().sendMessage("You are already in this lobby.").queue();
                                return;
                            }
                        }
                        lobby.addPlayer(user.getId(), user.getName());

                        updateLobbyEmbed(event, lobby);
                    }
                }
                case "leaveLobby" -> {
                    Lobby lobby2 = Lobby.lobbyInstances.get(event.getMessage().getId());
                    LobbyEntry lobbyEntryToRemove = null;
                    if (lobby2 != null) {
                        List<LobbyEntry> players2 = lobby2.getPlayers();
                        for (LobbyEntry lobbyEntry : players2) {
                            if (lobbyEntry.userId().equals(user.getId())) {
                                lobbyEntryToRemove = lobbyEntry;
                                break;
                            }
                        }
                    }
                    if (lobbyEntryToRemove != null) {
                        lobby2.removePlayer(lobbyEntryToRemove);

                        updateLobbyEmbed(event, lobby2);
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
                Paginator paginator = Paginator.paginatorInstances.get(event.getMessage().getId());
                if (paginator != null) {
                    paginator.nextPage().ifPresent(embed -> event.getHook().editOriginalEmbeds(embed.build()).queue());
                }
                break;
            case "previousPage":
                Paginator paginator2 = Paginator.paginatorInstances.get(event.getMessage().getId());
                if (paginator2 != null) {
                    paginator2.previousPage().ifPresent(embed -> event.getHook().editOriginalEmbeds(embed.build()).queue());
                }
                break;
            case "regenerate":
                MessageEmbed build = encCmd.regenerateEncounter(event.getMessage().getEmbeds().get(0), event.getUser());
                event.getHook().editOriginalEmbeds(build).setActionRows(
                        ActionRow.of(Button.primary(event.getUser().getId() + ":regenerate", "Regenerate"),
                                Button.primary(event.getUser().getId() + ":save", "Save"),
                                Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                break;
            case "save":
                event.getHook().editOriginalEmbeds(event.getMessage().getEmbeds()).setActionRows(ActionRow.of(
                        Button.primary(event.getUser().getId() + ":regenerate", "Regenerate"),
                        Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                encCmd.saveEncounter(event.getMessage().getEmbeds().get(0), event.getUser());
                break;
            case "hit":
                Blackjack game = BlackjackPlayCmd.blackjackGames.get(event.getUser().getId());
                if (game.isFinished() || game.isPlayerStand()) {
                    break;
                }
                game.playerHit();
                BlackjackStates blackjackStates = game.checkWin(false);
                EmbedBuilder newEmbed;
                if (blackjackStates.equals(BlackjackStates.DEALER_WIN)) {
                    game.checkWin(true);
                    newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                    event.getHook().editOriginalEmbeds(newEmbed.build()).setActionRows(ActionRow.of(
                            Button.primary(event.getUser().getId() + ":replayBlackjack", "Replay"),
                            Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
                    BlackjackPlayCmd.blackjackGames.remove(user.getId());
                } else {
                    newEmbed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), null);
                    event.getHook().editOriginalEmbeds(newEmbed.build()).queue();
                }
                break;
            case "stand":
                Blackjack blackjack = BlackjackPlayCmd.blackjackGames.get(event.getUser().getId());
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
                BlackjackPlayCmd.blackjackGames.remove(user.getId());
                break;
            case "replayBlackjack":
                if (BlackjackPlayCmd.blackjackGames.containsKey(authorId)) {
                    break;
                }
                String description = event.getMessage().getEmbeds().get(0).getDescription();
                Blackjack value;
                if (description == null) {
                    value = new Blackjack(authorId);
                } else {
                    String s = description.replaceAll("[^0-9]", "");
                    int bet = Integer.parseInt(s);
                    ArrayList<String> result = dbManager.query(UsersTableQueries.getUserCurrency, DatabaseManager.QueryTypes.RETURN, user.getId());
                    int wallet = Integer.parseInt(result.get(0));
                    if (bet > wallet) {
                        event.getHook().sendMessage(String.format("You can't bet `%d` morbcoins, you only have `%d` in your wallet.", bet, wallet)).queue();
                        break;
                    }
                    value = new Blackjack(authorId, bet);
                }
                value.initializeGame();
                BlackjackPlayCmd.blackjackGames.put(event.getUser().getId(), value);
                BlackjackStates state = value.checkWin(false);
                EmbedBuilder embed;
                if (state.equals(BlackjackStates.PLAYER_BLACKJACK)) {
                    value.dealerHit();
                    value.setDealerStand(true);
                    blackjackStates = value.checkWin(true);
                    embed = BlackjackPlayCmd.generateBlackjackEmbed(event.getUser(), blackjackStates);
                    BlackjackPlayCmd.blackjackGames.remove(authorId);
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
            case "fillLobby":
                Lobby filledLobby = Lobby.lobbyInstances.get(event.getMessage().getId());
                int maxPlayers = filledLobby.getMaxPlayers();
                int size = filledLobby.getPlayers().size();
                if (size >= maxPlayers) {
                    event.getHook().sendMessage("This lobby is already full.").queue();
                } else {
                    filledLobby.fillLobby();
                    updateLobbyEmbed(event, filledLobby);
                }
                break;
            case "startHg":
                Lobby lobby3 = Lobby.lobbyInstances.get(event.getMessage().getId());
                lobby3.destroy();
                HungerGames hungerGames = new HungerGames(lobby3.getPlayers());
                hungerGames.startGame();
                HungerGamesStartCmd.runGame(event, hungerGames);
                break;
        }

    }

    private void updateLobbyEmbed(@NotNull ButtonClickEvent event, @NotNull Lobby lobby2) {
        MessageEmbed messageEmbed = event.getMessage().getEmbeds().get(0);
        String title = messageEmbed.getTitle();

        EmbedBuilder embedBuilder2 = new EmbedBuilder();
        embedBuilder2.setTitle(title);
        embedBuilder2.setTimestamp(new Date().toInstant());
        embedBuilder2.setColor(Color.BLUE);
        embedBuilder2.setDescription(lobby2.generateDescription());

        event.getHook().editOriginalEmbeds(embedBuilder2.build()).queue();
    }
}