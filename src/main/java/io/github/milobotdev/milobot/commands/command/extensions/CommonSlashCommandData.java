package io.github.milobotdev.milobot.commands.command.extensions;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CommonSlashCommandData {

    SlashCommandData slashCommandData;
    SubcommandData subcommandData;

    public CommonSlashCommandData(SlashCommandData slashCommandData) {
        this.slashCommandData = slashCommandData;
    }

    public CommonSlashCommandData(SubcommandData subcommandData) {
        this.subcommandData = subcommandData;
    }

    @NotNull
    public String getName() {
        if (slashCommandData != null) {
            return slashCommandData.getName();
        } else {
            return subcommandData.getName();
        }
    }

    @NotNull
    public String getDescription() {
        if (slashCommandData != null) {
            return slashCommandData.getDescription();
        } else {
            return subcommandData.getDescription();
        }
    }

    @NotNull
    public SlashCommandData getSlashCommandData() {
        if (slashCommandData == null) {
            throw new IllegalStateException("This is a parent command slash command");
        }
        return slashCommandData;
    }

    @NotNull
    public SubcommandData getSubcommandData() {
        if (subcommandData == null) {
            throw new IllegalStateException("This is a sub command slash command");
        }
        return subcommandData;
    }
}
