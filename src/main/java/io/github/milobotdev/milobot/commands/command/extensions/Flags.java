package io.github.milobotdev.milobot.commands.command.extensions;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface Flags extends TextCommand {

    @NotNull Set<String> getFlags();
    void executeFlag(@NotNull MessageReceivedEvent event, @NotNull String flag);
}