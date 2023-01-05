package io.github.milobotdev.milobot.commands.instance.model;

import io.github.milobotdev.milobot.utility.TimeTracker;
import net.dv8tion.jda.api.entities.Message;

public record CancelMessageData(GameType gameType, Message cancelMessage, TimeTracker timeTracker) {
}
