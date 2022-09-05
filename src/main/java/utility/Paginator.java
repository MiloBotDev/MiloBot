package utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import utility.lobby.AbstractLobby;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Paginator {

    private final static Map<Message, Paginator> paginatorInstances = new HashMap<>();
    private final List<MessageEmbed> pages;
    private int currentPage;
    private static final ScheduledExecutorService idleInstanceCleanupExecutorService =
            Executors.newScheduledThreadPool(1);
    protected volatile Message message;
    private ScheduledFuture<?> idleInstanceCleanupFuture;
    private volatile boolean initialized = false;


    public Paginator() {
        pages = new ArrayList<>();
        this.currentPage = 0;
    }

    public void initialize(Message message) {
        this.message = message;
        setIdleInstanceCleanup();
        initialized = true;
        paginatorInstances.put(message, this);
    }

    protected final boolean cancelIdleInstanceCleanup() {
        return idleInstanceCleanupFuture.cancel(false);
    }

    protected final void setIdleInstanceCleanup() {
        idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
            paginatorInstances.remove(message);
            message.delete().queue();
        }, 15, TimeUnit.MINUTES);
    }

    protected final void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Lobby not initialized");
        }
    }

    public void addPages(List<MessageEmbed> embeds) {
        pages.addAll(embeds);
    }

    public void nextPage() {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (currentPage + 1 < pages.size()) {
            currentPage++;
            message.editMessageEmbeds(pages.get(currentPage)).queue();
        }
        setIdleInstanceCleanup();
    }

    public void previousPage() {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (currentPage - 1 >= 0) {
            currentPage--;
            message.editMessageEmbeds(pages.get(currentPage)).queue();
        }
        setIdleInstanceCleanup();
    }

    public static Paginator getPaginatorByMessage(Message message) {
        return paginatorInstances.get(message);
    }

    public void remove() {
        if (cancelIdleInstanceCleanup()) {
            paginatorInstances.remove(message);
            message.delete().queue();
        }
    }

    public MessageEmbed currentPage() {
        return pages.get(currentPage);
    }
}
