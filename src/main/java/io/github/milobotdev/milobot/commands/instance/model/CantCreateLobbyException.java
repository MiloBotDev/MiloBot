package io.github.milobotdev.milobot.commands.instance.model;

public class CantCreateLobbyException extends Exception {
    public CantCreateLobbyException(String errorMessage) {
        super(errorMessage);
    }
}
