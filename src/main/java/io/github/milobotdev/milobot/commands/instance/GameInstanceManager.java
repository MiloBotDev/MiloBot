package io.github.milobotdev.milobot.commands.instance;

import io.github.milobotdev.milobot.commands.instance.model.GameInstanceData;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.main.JDAManager;
import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static io.github.milobotdev.milobot.commands.command.extensions.Instance.cancelGameInstances;

public class GameInstanceManager {

    private final Map<Long, GameInstanceData> gameInstances = new ConcurrentHashMap<>();
    private static GameInstanceManager instance = null;
    private static final ScheduledExecutorService idleInstanceCleanupExecutorService =
            Executors.newScheduledThreadPool(1);

    private GameInstanceManager() {
        this.setIdleInstanceCleanup();
    }

    public static GameInstanceManager getInstance() {
        if (instance == null) {
            instance = new GameInstanceManager();
        }
        return instance;
    }

    public void initialize() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                if(event.getAuthor().isBot()) {
                    return;
                }
                if(event.getMessage().getContentRaw().equalsIgnoreCase("cancel")) {
                    cancelGameInstances.forEach((userId, cancelMessageData) -> {
                        if (userId == event.getAuthor().getIdLong()) {
                            GameType gameType = gameInstances.get(userId).gameType();
                            gameType.removeInstanceMethod().removeGame(userId);
                            removeUserGame(userId);
                            cancelGameInstances.remove(userId);
                            event.getMessage().reply(String.format("You have been removed from your %s game.", gameType.name())).queue();
                        }
                    });
                }
            }
        });
    }

    public boolean containsUser(long userId) {
        return gameInstances.containsKey(userId);
    }

    public void addUser(long userId, GameType gameType, int duration) {
        if (containsUser(userId)) {
            throw new IllegalStateException("Tried adding a user to a game instance that already exists.");
        }
        TimeTracker tracker = new TimeTracker(duration);
        tracker.start();
        this.gameInstances.put(userId, new GameInstanceData(gameType, tracker));
    }

    public void removeUserGame(long userId) {
        gameInstances.remove(userId);
    }

    public TimeTracker getUserTimeTracker(long userId) {
        return gameInstances.get(userId).timeTracker();
    }

    private void setIdleInstanceCleanup() {
        idleInstanceCleanupExecutorService.scheduleWithFixedDelay(() -> {
            gameInstances.forEach((userId, gameInstanceData) -> {
                if (gameInstanceData.timeTracker().isTimeSecondsPastDuration()) {
                    gameInstances.remove(userId);
                }
            });

            cancelGameInstances.forEach((userId, cancelMessageData) -> {
                if(cancelMessageData.timeTracker().isTimeSecondsPastDuration()) {
                    cancelGameInstances.remove(userId);
                }
            });
        }, 10, 10, TimeUnit.SECONDS);
    }

}
