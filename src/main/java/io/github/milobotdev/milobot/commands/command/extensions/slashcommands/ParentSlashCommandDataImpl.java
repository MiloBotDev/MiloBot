package io.github.milobotdev.milobot.commands.command.extensions.slashcommands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public class ParentSlashCommandDataImpl implements ParentSlashCommandData {

    private final SlashCommandData slashCommandData;

    public ParentSlashCommandDataImpl(SlashCommandData slashCommandData) {
        this.slashCommandData = slashCommandData;
    }

    @Override
    public @NotNull SlashCommandData getSlashCommandData() {
        return slashCommandData;
    }

    @Override
    public @NotNull String getName() {
        return slashCommandData.getName();
    }

    @Override
    public @NotNull String getDescription() {
        return slashCommandData.getDescription();
    }
}
