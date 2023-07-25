package io.github.milobotdev.milobot.commands.command.extensions.slashcommands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public class SlashCommandDataUtils {

    @NotNull
    public static ParentSlashCommandData fromSlashCommandData(SlashCommandData data) {
        return new ParentSlashCommandDataImpl(data);
    }

    @NotNull
    public static SubSlashCommandData fromSubCommandData(SubcommandData data) {
        return new SubSlashCommandDataImpl(data);
    }
}
