package io.github.milobotdev.milobot.commands.command.extensions.slashcommands;

import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

public interface SubSlashCommandData extends CommonSlashCommandData {

    @NotNull SubcommandData getSubSlashCommandData();
}
