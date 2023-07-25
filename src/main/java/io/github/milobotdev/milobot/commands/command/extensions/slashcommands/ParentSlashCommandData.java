package io.github.milobotdev.milobot.commands.command.extensions.slashcommands;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;

public interface ParentSlashCommandData extends CommonSlashCommandData {

    @NotNull SlashCommandData getSlashCommandData();
}
