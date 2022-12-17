package io.github.milobotdev.milobot.commands.instance;

import io.github.milobotdev.milobot.utility.TimeTracker;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameInstanceManager {

    private final Map<Map<Long, GameType>, TimeTracker> gameInstances;
    private static GameInstanceManager instance = null;
    private static final ScheduledExecutorService idleInstanceCleanupExecutorService =
            Executors.newScheduledThreadPool(1);

    private GameInstanceManager() {
        this.gameInstances = new HashMap<>();
        this.setIdleInstanceCleanup();
    }

    public static GameInstanceManager getInstance() {
        if(instance == null) {
            instance = new GameInstanceManager();
        }
        return instance;
    }

    public boolean containsUser(long userId, GameType gameType) {
        final boolean[] contains = {false};
        gameInstances.forEach((longStringMap, timeTracker) -> {
            if(longStringMap.containsKey(userId) && longStringMap.containsValue(gameType)) {
                contains[0] = true;
            }
        });
        return contains[0];
    }

    public void addUser(long userId, GameType gameType, int duration) {
        if(containsUser(userId, gameType)) {
            throw new IllegalStateException("Tried adding a user to a game instance that already exists.");
        }
        TimeTracker tracker = new TimeTracker(duration);
        tracker.start();
        this.gameInstances.put(Map.of(userId, gameType), tracker);
    }

    public void removeUserGame(long userId, GameType gameType) {
        final Collection<Map<Long, GameType>> keyToRemove = new HashSet<>();
        this.gameInstances.forEach((longStringMap, timeTracker) -> {
            if(longStringMap.containsKey(userId) && longStringMap.containsValue(gameType)) {
                keyToRemove.add(longStringMap);
            }
        });
        if(keyToRemove.isEmpty()) {
            throw new IllegalStateException("Tried to remove user game that doesn't exist");
        }
        gameInstances.remove(keyToRemove.iterator().next());
    }

    public TimeTracker getUserTimeTracker(long userId, GameType gameType) {
        final TimeTracker[] returnValue = new TimeTracker[1];
        this.gameInstances.forEach((longStringMap, timeTracker) -> {
            if(longStringMap.containsKey(userId) && longStringMap.containsValue(gameType)) {
                returnValue[0] = timeTracker;
            }
        });
        if(Arrays.stream(returnValue).sequential().allMatch(Objects::isNull)) {
            throw new IllegalStateException("Tried to get user time tracker for a game that doesn't exist");
        }
        return returnValue[0];
    }

    private void setIdleInstanceCleanup() {
        idleInstanceCleanupExecutorService.schedule(() -> {
            gameInstances.forEach((longGameTypeMap, timeTracker) -> {
                if (timeTracker.isTimeSecondsPastDuration()) {
                    gameInstances.remove(longGameTypeMap);
                }
            });
        }, 1, TimeUnit.MINUTES);
    }

}
