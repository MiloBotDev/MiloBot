package utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

public class NewLobby {

    private static final Map<Message, NewLobby> lobbyInstances = new HashMap<>();
    private static final ScheduledExecutorService idleInstanceCleanupExecutorService =
            Executors.newScheduledThreadPool(1);
    private final Set<User> players = new HashSet<>();
    private final Consumer<List<User>> startConsumer;
    private final int minPlayers;
    private final int maxPlayers;
    private final String title;
    private final User creator;
    private boolean started = false;
    private ScheduledFuture<?> idleInstanceCleanupFuture;
    private volatile Message message;
    private volatile boolean initialized = false;

    public NewLobby(String title, User creator, Consumer<List<User>> startConsumer,
                    int minPlayers, int maxPlayers) {
        this.creator = creator;
        players.add(this.creator);
        this.title = title;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.startConsumer = startConsumer;
    }

    /**
     * Initializes the lobby with the lobby message. All subsequent calls on this class MUST be from the same thread.
     *
     * @param channel Channel of lobby.
     */
    public void initialize(MessageChannel channel) {
        if (initialized) {
            throw new IllegalStateException("Lobby already initialized.");
        }
        channel.sendMessageEmbeds(getEmbed()).setActionRows(getEmbedActionsRows()).queue(this::initialize);
    }

    private void initialize(Message message) {
        this.message = message;
        setIdleInstanceCleanup();
        initialized = true;
        lobbyInstances.put(message, this);
    }

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

    private boolean cancelIdleInstanceCleanup() {
        return idleInstanceCleanupFuture.cancel(false);
    }

    private void setIdleInstanceCleanup() {
        idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
            lobbyInstances.remove(message);
            message.delete().queue();
        }, 15, TimeUnit.SECONDS);
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Lobby not initialized");
        }
    }

    private void editMessage() {
        message.editMessageEmbeds(getEmbed()).setActionRows(getEmbedActionsRows()).queue();
    }

    private ActionRow getEmbedActionsRows() {
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

    private MessageEmbed getEmbed() {
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

    public static NewLobby getLobbyByMessage(Message message) {
        return lobbyInstances.get(message);
    }

    public static void removeLobbyByMessage(Message message) {
        NewLobby lobby = lobbyInstances.get(message);
        if (lobby != null) {
            if (lobby.cancelIdleInstanceCleanup()) {
                lobbyInstances.remove(message);
                message.delete().queue();
            }
        }
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
