package io.github.milobotdev.milobot.commands.command.extensions;

import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

/**
 * This interface provides a default implementation for getAllowedChannelTypes().
 * This interface should only be implemented by commands that can be used anywhere.
 * @see TextCommand
 */
public interface DefaultChannelTypes extends TextCommand {

    @Override
    default @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.TEXT, ChannelType.PRIVATE);
    }
}
