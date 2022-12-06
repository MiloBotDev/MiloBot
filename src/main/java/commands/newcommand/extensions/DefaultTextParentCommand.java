package commands.newcommand.extensions;

import commands.GuildPrefixManager;
import commands.newcommand.IParentCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import utility.Config;
import utility.EmbedUtils;

import java.util.List;

public interface DefaultTextParentCommand extends TextCommand, IParentCommand {
    private void sendCommandExplanation(MessageReceivedEvent event, String prefix) {
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
    default void executeCommand(MessageReceivedEvent event, List<String> args) {
        sendCommandExplanation(event, event.isFromGuild() ?
                GuildPrefixManager.getInstance().getPrefix(event.getGuild().getIdLong()) :
                Config.getInstance().getPrivateChannelPrefix());
    }

    @Override
    default boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        return true;
    }

    @Override
    default List<String> getCommandArgs() {
        return List.of();
    }
}
