package io.github.milobotdev.milobot.commands.command.extensions;

import io.github.milobotdev.milobot.commands.GuildPrefixManager;
import io.github.milobotdev.milobot.commands.command.IParentCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import io.github.milobotdev.milobot.utility.Config;
import io.github.milobotdev.milobot.utility.EmbedUtils;

import java.util.List;

public interface DefaultTextParentCommand extends TextCommand, IParentCommand {

    private void sendCommandExplanation(@NotNull MessageReceivedEvent event, @NotNull String prefix) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, event.getAuthor());
        embed.setTitle(getCommandName());
        embed.setDescription("This is the base command for all " + getCommandName() + " related commands. Please use any of the " +
                "commands listed below.");
        embed.addField("Sub Commands", getSubCommandsText(prefix), false);
        event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    @Override
    default void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        sendCommandExplanation(event, event.isFromGuild() ?
                GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong()) :
                Config.getInstance().getPrivateChannelPrefix());
    }

    @Override
    default boolean checkRequiredArgs(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        return true;
    }

    @Override
    default @NotNull List<String> getCommandArgs() {
        return List.of();
    }
}
