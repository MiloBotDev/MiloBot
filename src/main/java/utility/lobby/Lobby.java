package utility.lobby;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.*;
import java.util.function.Consumer;

public class Lobby extends AbstractLobby {
    private final Set<User> players = new HashSet<>();
    private final Consumer<List<User>> startConsumer;
    private final int minPlayers;
    private final int maxPlayers;
    private final String title;
    private final User creator;
    private boolean started = false;

    public Lobby(String title, User creator, Consumer<List<User>> startConsumer,
                 int minPlayers, int maxPlayers) {
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
            if (players.add(user)) {
                editMessage();
            } else {
                message.reply(user.getAsMention() + " You are already in this lobby.").queue();
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
            if (players.remove(user)) {
                editMessage();
            } else {
                message.reply(user.getAsMention() + " You are not in this lobby.").queue();
            }
        } else {
            message.reply(user.getAsMention() + " You can't leave your own lobby.").queue();
        }
        setIdleInstanceCleanup();
    }

    private void editMessage() {
        message.editMessageEmbeds(getEmbed()).setActionRows(getEmbedActionsRows()).queue();
    }

    @Override
    protected ActionRow getEmbedActionsRows() {
        Button joinButton;
        if (players.size() == maxPlayers) {
            joinButton = Button.danger(creator.getId() + ":" + "joinNewLobby", "Join");
        } else {
            joinButton = Button.primary(creator.getId() + ":" + "joinNewLobby", "Join");
        }
        Button leaveButton = Button.primary(creator.getId() + ":" + "leaveNewLobby", "Leave");
        Button startButton = Button.primary(creator.getId() + ":" + "startNewLobby", "Start");
        Button deleteButton = Button.secondary(creator.getId() + ":" + "deleteNewLobby", "Delete");
        return ActionRow.of(joinButton, leaveButton, startButton, deleteButton);
    }

    @Override
    protected MessageEmbed getEmbed() {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
        eb.setDescription(generateDescription());
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
        if (players.size() >= minPlayers) {
            if (!started) {
                startConsumer.accept(players.stream().toList());
                started = true;
            }
        } else {
            message.reply("Not enough players to start the lobby.").queue();
        }
    }
}
