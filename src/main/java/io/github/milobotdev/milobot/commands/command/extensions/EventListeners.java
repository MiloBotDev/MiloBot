package io.github.milobotdev.milobot.commands.command.extensions;

import net.dv8tion.jda.api.hooks.EventListener;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface EventListeners {

    @NotNull List<EventListener> getEventListeners();
}
