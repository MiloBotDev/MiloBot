package utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.*;
import java.util.function.Consumer;

public class NewLobby {
    private static final Map<Long, NewLobby> lobbyInstances = new HashMap<>();
    private final Set<User> players = new HashSet<>();
    private final Consumer<List<User>> startConsumer;
    private final int minPlayers;
    private final int maxPlayers;
    private final String title;
    private final User creator;
    private boolean started = false;

    public NewLobby(String title, User creator, Consumer<List<User>> startConsumer,
                    int minPlayers, int maxPlayers) {
        players.add(creator);
        this.creator = creator;
        this.title = title;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startConsumer = startConsumer;
    }

    public void initialize(long id) {
        lobbyInstances.put(id, this);
    }

    public boolean addPlayer(User user) {
        if (players.size() < maxPlayers) {
            return players.add(user);
        }
        return true;
    }

    public boolean removePlayer(User user) {
        return players.remove(user);
    }

    public ActionRow getEmbedActionsRows() {
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

    public MessageEmbed getEmbed() {
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
        for (net.dv8tion.jda.api.entities.User user : players) {
            sb.append("- ").append(user.getAsMention()).append("\n");
        }
        return sb.toString();
    }

    public static NewLobby getLobbyById(long id) {
        return lobbyInstances.get(id);
    }

    public static void removeLobbyById(long id) {
        lobbyInstances.remove(id);
    }

    public User getCreator() {
        return creator;
    }

    public boolean start() {
        if (players.size() >= minPlayers) {
            if (!started) {
                startConsumer.accept(players.stream().toList());
                started = true;
            }
            return true;
        } else {
            return false;
        }
    }
}
