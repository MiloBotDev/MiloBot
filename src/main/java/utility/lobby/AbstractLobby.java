package utility.lobby;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractLobby {
    protected static final Map<Message, AbstractLobby> lobbyInstances = new HashMap<>();
    private static final ScheduledExecutorService idleInstanceCleanupExecutorService =
            Executors.newScheduledThreadPool(1);
    protected volatile Message message;
    private ScheduledFuture<?> idleInstanceCleanupFuture;
    private volatile boolean initialized = false;

    /**
     * Initializes the lobby with the lobby message. All subsequent outside method calls on this class and static
     * methods calls intending to operate on this lobby MUST be from the same thread.
     *
     * @param channel Channel of lobby.
     */
    public final void initialize(MessageChannel channel) {
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

    protected final boolean cancelIdleInstanceCleanup() {
        return idleInstanceCleanupFuture.cancel(false);
    }

    protected final void setIdleInstanceCleanup() {
        idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
            lobbyInstances.remove(message);
            message.delete().queue();
        }, 15, TimeUnit.MINUTES);
    }

    protected final void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Lobby not initialized");
        }
    }

    public static AbstractLobby getLobbyByMessage(Message message) {
        return lobbyInstances.get(message);
    }

    public final void remove() {
        if (cancelIdleInstanceCleanup()) {
            lobbyInstances.remove(message);
            message.delete().queue();
        }
    }

    protected void editMessage() {
        ActionRow actionRow = getEmbedActionsRows();
        if (actionRow != null) {
            message.editMessageEmbeds(getEmbed()).setActionRows(actionRow).queue();
        } else {
            message.editMessageEmbeds(getEmbed()).setActionRows().queue();
        }
    }

    protected abstract MessageEmbed getEmbed();

    protected abstract ActionRow getEmbedActionsRows();

    public abstract void addPlayer(User user);

    public abstract void removePlayer(User user);

    public abstract void start();
}
