package io.github.milobotdev.milobot.utility.lobby;

import io.github.milobotdev.milobot.commands.instance.LobbyInstanceManager;
import io.github.milobotdev.milobot.commands.instance.model.CantCreateLobbyException;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ISnowflake;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

public class Lobby extends AbstractLobby {

    private final Set<User> players = new HashSet<>();
    private final BiConsumer<List<User>, Message> startConsumer;
    private final int minPlayers;
    private final int maxPlayers;
    private final String title;
    private final User creator;
    private boolean started = false;
    private final LobbyInstanceManager lobbyInstanceManager = LobbyInstanceManager.getInstance();


    public Lobby(String title, User creator, BiConsumer<List<User>, Message> startConsumer,
                 int minPlayers, int maxPlayers) throws CantCreateLobbyException {
        if(lobbyInstanceManager.isUserInLobby(creator.getIdLong())) {
            throw new CantCreateLobbyException("Can't create a new lobby when you are already in one.");
        }
        lobbyInstanceManager.addUser(creator.getIdLong());
        this.creator = creator;
        players.add(this.creator);
        this.title = title;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startConsumer = startConsumer;
    }

    @Override
    public void addPlayer(User user) {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (players.size() < maxPlayers) {
            long userIdLong = user.getIdLong();
            if (players.add(user) && !lobbyInstanceManager.isUserInLobby(userIdLong)) {
                editMessage();
            } else {
                players.stream()
                        .filter(u -> u.getIdLong() == userIdLong)
                        .findFirst()
                        .ifPresentOrElse(u -> message.reply(u.getAsMention() + " You are already in this lobby.").queue(),
                                () -> message.reply(user.getAsMention() + " You are already in another lobby.").queue());
            }
        }
        setIdleInstanceCleanup();
    }

    @Override
    public void removePlayer(User user) {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (!user.equals(creator)) {
            if (players.remove(user) && lobbyInstanceManager.isUserInLobby(user.getIdLong())) {
                lobbyInstanceManager.removeUser(user.getIdLong());
                editMessage();
            } else {
                message.reply(user.getAsMention() + " You are not in this lobby.").queue();
            }
        } else {
            message.reply(user.getAsMention() + " You can't leave your own lobby.").queue();
        }
        setIdleInstanceCleanup();
    }

    @Override
    protected ActionRow getEmbedActionsRows() {
        ActionRow ret;
        if (started) {
            ret = null;
        } else {
            Button joinButton;
            if (players.size() == maxPlayers) {
                joinButton = Button.danger(creator.getId() + ":" + "joinLobby", "Join");
            } else {
                joinButton = Button.primary(creator.getId() + ":" + "joinLobby", "Join");
            }
            Button leaveButton = Button.primary(creator.getId() + ":" + "leaveLobby", "Leave");
            Button startButton = Button.primary(creator.getId() + ":" + "startLobby", "Start");
            Button deleteButton = Button.secondary(creator.getId() + ":" + "deleteLobby", "Delete");
            ret = ActionRow.of(joinButton, leaveButton, startButton, deleteButton);
        }
        return ret;
    }

    @Override
    protected MessageEmbed getEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setDescription(generateDescription());
        eb.setColor(Color.BLUE);
        eb.setTimestamp(new Date().toInstant());
        return eb.build();
    }

    private String generateDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append("**Minimum Players:** ").append(this.minPlayers).append("\n");
        sb.append("**Maximum Players:** ").append(this.maxPlayers).append("\n");
        sb.append("**Current Players:** \n");
        for (User user : players) {
            sb.append("- ").append(user.getAsMention()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public void start() {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (players.size() >= minPlayers) {
            lobbyInstances.remove(message);
            started = true;
            editMessage();
            startConsumer.accept(players.stream().toList(), message);
            removePlayersFromInstanceManager();
        } else {
            message.reply("Not enough players to start the lobby.").queue();
            setIdleInstanceCleanup();
        }
    }

    public void removePlayersFromInstanceManager() {
        players.stream().mapToLong(ISnowflake::getIdLong).forEach(idLong -> {
            if (lobbyInstanceManager.isUserInLobby(idLong)) {
                lobbyInstanceManager.removeUser(idLong);
            }
        });
    }
}
