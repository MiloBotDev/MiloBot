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
            channel.sendMessage(String.format("You are still in game. Please wait %d more seconds, " +
                            "or type cancel within 10 seconds to leave the game.",
                    userTimeTracker.timeSecondsTillDuration())).queue(
                    message -> {
                        if (!cancelGameInstances.containsKey(userId)) {
                            CancelMessageData cancelMessageData = new CancelMessageData(gameType, message,
                                    new TimeTracker(10));
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
