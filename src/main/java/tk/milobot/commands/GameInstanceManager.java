package tk.milobot.commands;

import tk.milobot.utility.TimeTracker;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class GameInstanceManager {

    private final Map<Map<Long, String>, TimeTracker> gameInstances;
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

    public boolean containsUser(long userId, String gameName) {
        final boolean[] contains = {false};
        gameInstances.forEach((longStringMap, timeTracker) -> {
            if(longStringMap.containsKey(userId) && longStringMap.containsValue(gameName)) {
                contains[0] = true;
            }
        });
        return contains[0];
    }

    public void addUser(long userId, String gameName, int duration) {
        if(containsUser(userId, gameName)) {
            throw new IllegalStateException("Tried adding a user to a game instance that already exists.");
        }
        TimeTracker tracker = new TimeTracker(duration);
        tracker.start();
        this.gameInstances.put(Map.of(userId, gameName), tracker);
    }

    public void removeUserGame(long userId, String gameName) {
        final Collection<Map<Long, String>> keyToRemove = new HashSet<>();
        this.gameInstances.forEach((longStringMap, timeTracker) -> {
            if(longStringMap.containsKey(userId) && longStringMap.containsValue(gameName)) {
                keyToRemove.add(longStringMap);
            }
        });
        if(keyToRemove.isEmpty()) {
            throw new IllegalStateException("Tried to remove user game that doesn't exist");
        }
        gameInstances.remove(keyToRemove.iterator().next());
    }

    public TimeTracker getUserTimeTracker(long userId, String gameName) {
        final TimeTracker[] returnValue = new TimeTracker[1];
        this.gameInstances.forEach((longStringMap, timeTracker) -> {
            if(longStringMap.containsKey(userId) && longStringMap.containsValue(gameName)) {
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
            gameInstances.forEach((aLong, timeTracker) -> {
                if(timeTracker.isTimeSecondsPastDuration()) {
                    gameInstances.remove(aLong);
                }
            });
        }, 1, TimeUnit.MINUTES);
    }

}
