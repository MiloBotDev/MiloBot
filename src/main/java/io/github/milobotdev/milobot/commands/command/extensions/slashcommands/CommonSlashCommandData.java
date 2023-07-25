package io.github.milobotdev.milobot.commands.command.extensions.slashcommands;

import org.jetbrains.annotations.NotNull;

public interface CommonSlashCommandData {

    @NotNull String getName();
    @NotNull String getDescription();
}
