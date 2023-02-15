package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.commands.instance.model.CancelMessageData;
import io.github.milobotdev.milobot.commands.instance.model.GameType;
import io.github.milobotdev.milobot.commands.instance.model.InstanceData;
import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public interface Instance {

    Map<Long, CancelMessageData> cancelGameInstances = new ConcurrentHashMap<>();

    InstanceData isInstanced();

    default boolean manageInstance(MessageChannel channel, @NotNull User author, GameType gameType, int duration) {
        GameInstanceManager gameInstanceManager = GameInstanceManager.getInstance();
        long userId = author.getIdLong();
        if (gameInstanceManager.containsUser(userId)) {
            TimeTracker userTimeTracker = gameInstanceManager.getUserTimeTracker(userId);
            long waitTime = userTimeTracker.timeSecondsTillDuration();
            // wait time can never be negative
            if(waitTime <= 0 ) {
                gameInstanceManager.removeUserGame(userId);
                gameInstanceManager.addUser(userId, gameType, duration);
                return false;
            }
            channel.sendMessage(String.format("You are still in game. Please wait %d more seconds, " +
                            "or type cancel within 10 seconds to leave the game.",
                    waitTime)).queue(
                    message -> {
                        if (!cancelGameInstances.containsKey(userId)) {
                            TimeTracker timeTracker = new TimeTracker(10);
                            CancelMessageData cancelMessageData = new CancelMessageData(gameType, message,
                                    timeTracker);
                            timeTracker.start();
                            cancelGameInstances.put(userId, cancelMessageData);
                        }
                    });
            return true;
        } else {
            gameInstanceManager.addUser(userId, gameType, duration);
            return false;
        }
    }



}
