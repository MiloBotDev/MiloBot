package io.github.milobotdev.milobot.commands.instance.model;

import io.github.milobotdev.milobot.utility.TimeTracker;

public record GameInstanceData(GameType gameType, TimeTracker timeTracker) {
}
