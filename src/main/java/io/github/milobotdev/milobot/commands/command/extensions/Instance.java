package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.commands.instance.GameType;
import io.github.milobotdev.milobot.commands.instance.InstanceData;
import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public interface Instance {

    Map<Map<Long, GameType>, Map<Message, TimeTracker>> cancelGameInstances = new ConcurrentHashMap<>();
    ScheduledExecutorService cancelGameInstanceService = Executors.newScheduledThreadPool(1);
    InstanceData isInstanced();

    default void manageInstance(MessageChannel channel, @NotNull User author, GameType gameType, int duration) {
        GameInstanceManager gameInstanceManager = GameInstanceManager.getInstance();
        long userId = author.getIdLong();
        if (gameInstanceManager.containsUser(userId, gameType)) {
            TimeTracker userTimeTracker = gameInstanceManager.getUserTimeTracker(userId, gameType);
            channel.sendMessage(String.format("You are still in game. Please wait %d more seconds, " +
                            "or react to this message within 10 seconds to leave the game.",
                    userTimeTracker.timeSecondsTillDuration())).queue(
                    message -> cancelGameInstances.put(Map.of(userId, gameType),
                            Map.of(message, new TimeTracker(10))));
        } else {
            gameInstanceManager.addUser(userId, gameType, duration);
        }
    }

    private void setCancelInstanceService() {
        GameInstanceManager gameInstanceManager = GameInstanceManager.getInstance();
        List<Map<Long, GameType>> toRemove = new ArrayList<>();
        cancelGameInstanceService.schedule(() -> {
            cancelGameInstances.forEach((longGameTypeMap, messageTimeTrackerMap) -> {
                long userId = longGameTypeMap.keySet().stream().findFirst().get();
                GameType gameType = longGameTypeMap.values().stream().findFirst().get();
                Message message = messageTimeTrackerMap.keySet().stream().findFirst().get();
                TimeTracker timeTracker = messageTimeTrackerMap.values().stream().findFirst().get();
                if(!timeTracker.isTimeSecondsPastDuration()) {
                    message.getReactions().forEach(messageReaction -> messageReaction.retrieveUsers()
                            .queue(users -> users.forEach(user -> {
                                if (user.getIdLong() == userId) {
                                    gameInstanceManager.removeUserGame(userId, gameType);
                                    toRemove.add(longGameTypeMap);
                                }
                            })));
                } else {
                    toRemove.add(longGameTypeMap);
                }
            });
            toRemove.forEach(cancelGameInstances::remove);
        }, 1, TimeUnit.SECONDS);
    }
}
