package io.github.milobotdev.milobot.commands.command.extensions.slashcommands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public class SubSlashCommandDataImpl implements SubSlashCommandData {

    private final SubcommandData subSlashCommandData;

    public SubSlashCommandDataImpl(SubcommandData subSlashCommandData) {
        this.subSlashCommandData = subSlashCommandData;
    }

    @Override
    public @NotNull SubcommandData getSubSlashCommandData() {
        return subSlashCommandData;
    }

    @Override
    public @NotNull String getName() {
        return subSlashCommandData.getName();
    }

    @Override
    public @NotNull String getDescription() {
        return subSlashCommandData.getDescription();
    }
}
