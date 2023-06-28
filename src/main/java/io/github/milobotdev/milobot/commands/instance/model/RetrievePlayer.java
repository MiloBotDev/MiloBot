package io.github.milobotdev.milobot.commands.instance.model;

@FunctionalInterface
public interface RetrievePlayer {
    boolean isPlayerInGame(long userId);
}
