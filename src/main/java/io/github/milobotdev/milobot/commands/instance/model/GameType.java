package io.github.milobotdev.milobot.commands.instance.model;

public record GameType(String name, RemoveInstance removeInstanceMethod, boolean multiplayer, RetrievePlayer retrievePlayerMethod) {

}
