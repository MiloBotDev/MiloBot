package io.github.milobotdev.milobot.utility.lobby;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;

import java.util.Map;
import java.util.concurrent.*;

public abstract class AbstractLobby {

    protected static final Map<Message, AbstractLobby> lobbyInstances = new ConcurrentHashMap<>();
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
        channel.sendMessageEmbeds(getEmbed()).setActionRow(getEmbedActionsRows().getComponents()).queue(this::initialize);
    }

    /**
     * Initializes the lobby with the SlashCommandInteractionEvent. All subsequent outside method calls on this class and static
     * methods calls intending to operate on this lobby MUST be from the same thread.
     *
     * @param event The SlashCommandInteractionEvent that was triggered.
     */
    public final void initialize(SlashCommandInteractionEvent event) {
        if (initialized) {
            throw new IllegalStateException("Lobby already initialized.");
        }
        event.getHook().sendMessageEmbeds(getEmbed()).addActionRow(getEmbedActionsRows().getComponents()).queue(this::initialize);
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
            AbstractLobby lobby = lobbyInstances.get(message);
            if(lobby instanceof BotLobby botLobby) {
                botLobby.removePlayersFromInstanceManager();
            }
            if(lobby instanceof Lobby normalLobby) {
                normalLobby.removePlayersFromInstanceManager();
            }
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
            message.editMessageEmbeds(getEmbed()).setActionRow(actionRow.getComponents()).queue();
        } else {
            message.editMessageEmbeds(getEmbed()).setComponents().queue();
        }
    }

    protected abstract MessageEmbed getEmbed();

    protected abstract ActionRow getEmbedActionsRows();

    public abstract void addPlayer(User user);

    public abstract void removePlayer(User user);

    public abstract void start();
}
