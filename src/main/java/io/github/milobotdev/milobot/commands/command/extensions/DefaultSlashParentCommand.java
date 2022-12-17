package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.commands.command.IParentCommand;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import org.jetbrains.annotations.NotNull;

/**
 * This interface should only be used when a parent slash command has sub slash commands added to it.
 * This interface overrides the {@link #executeCommand(SlashCommandEvent)} to throw an exception,
 * because a parent slash command should never be executed when there are sub slash commands
 * added to it.
 * @see <a href="https://discord.com/developers/docs/interactions/application-commands#subcommands-and-subcommand-groups">Discord API docs on sub commands</a>
 */
public interface DefaultSlashParentCommand extends SlashCommand, IParentCommand {

    @Override
    default void executeCommand(@NotNull SlashCommandEvent event) {
        throw new IllegalStateException("This should never happen. When there are sub commands " +
                "added to slash command, the base command should become unusable.");
    }
}
