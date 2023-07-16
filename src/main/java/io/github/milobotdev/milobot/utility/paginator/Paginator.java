package io.github.milobotdev.milobot.utility.paginator;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

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
    private final User creator;

    public Paginator(User creator, List<MessageEmbed> pages) {
        this.pages = pages;
        this.currentPage = 0;
        this.creator = creator;
    }

    public Paginator() {
        this.pages = new ArrayList<>();
        this.currentPage = 0;
        this.creator = null;
    }

    public void initialize(Message message) {
        if (pages.size() > 1) {
            this.message = message;
            setIdleInstanceCleanup();
            initialized = true;
            paginatorInstances.put(message, this);
        }
    }

    private boolean cancelIdleInstanceCleanup() {
        return idleInstanceCleanupFuture.cancel(false);
    }

    private void setIdleInstanceCleanup() {
        idleInstanceCleanupFuture = idleInstanceCleanupExecutorService.schedule(() -> {
            paginatorInstances.remove(message);
            message.delete().queue();
        }, 15, TimeUnit.MINUTES);
    }

    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Lobby not initialized");
        }
    }

    protected int getCurrentPage() {
        return currentPage;
    }

    protected MessageAction getUpdateMessageAction() {
        return message.editMessageEmbeds(pages.get(currentPage)).setActionRows(getActionRows());
    }

    public void nextPage() {
        checkInitialized();
        if (!cancelIdleInstanceCleanup()) {
            return;
        }
        if (currentPage + 1 < pages.size()) {
            currentPage++;
            getUpdateMessageAction().queue();
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
            getUpdateMessageAction().queue();
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

    public ActionRow getActionRows() {
        Button previous = Button.primary(creator.getId() + ":previousPage", "Previous");
        Button next = Button.primary(creator.getId() + ":nextPage", "Next");
        Button delete;
        if (pages.size() > 1) {
            delete = Button.secondary(creator.getId() + ":deletePaginator", "Delete");
        } else {
            delete = Button.secondary(creator.getId() + ":delete", "Delete");
        }
        if (pages.size() == 1) {
            return ActionRow.of(delete);
        } if (currentPage == 0) {
            return ActionRow.of(next, delete);
        } else if (currentPage == pages.size() - 1) {
            return ActionRow.of(previous, delete);
        } else {
            return ActionRow.of(previous, next, delete);
        }
    }

    public MessageEmbed currentPage() {
        return pages.get(currentPage);
    }
}
