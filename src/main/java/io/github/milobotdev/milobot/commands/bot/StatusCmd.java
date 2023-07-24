package io.github.milobotdev.milobot.commands.bot;

import io.github.milobotdev.milobot.commands.command.extensions.DefaultChannelTypes;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultCommandArgs;
import io.github.milobotdev.milobot.commands.command.extensions.DefaultFlags;
import io.github.milobotdev.milobot.commands.command.extensions.TextCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import io.github.milobotdev.milobot.commands.command.ParentCommand;
import io.github.milobotdev.milobot.utility.EmbedUtils;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class StatusCmd extends ParentCommand implements TextCommand, DefaultFlags, DefaultCommandArgs, BotCmd, DefaultChannelTypes {

    private final ExecutorService executorService;

    public StatusCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull String getCommandName() {
        return "status";
    }

    @Override
    public @NotNull String getCommandDescription() {
        return "The status of the bot.";
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(embed, event.getAuthor());

        JDA jda = event.getJDA();
        List<Guild> guilds = jda.getGuilds();
        int amountOfGuilds = guilds.size();
        int memberTotal = guilds.stream().mapToInt(Guild::getMemberCount).sum();
        int emoteTotal = jda.getEmotes().size();
        long gatewayPing = jda.getGatewayPing();
        int categories = jda.getCategories().size();
        int textChannels = jda.getTextChannels().size();
        int voiceChannels = jda.getVoiceChannels().size();
        int roleTotal = jda.getRoles().size();

        embed.addField("Ping", String.format("The response time was %d milliseconds.", gatewayPing), false);
        embed.addField("Servers", String.format("The bot is running in %d servers for a total of %d members.",
                amountOfGuilds, memberTotal), false);
        embed.addField("Channels", String.format("The bot is looking for commands in %d text channels, " +
                        "in %d different categories and can see %d voice channels.", textChannels,
                categories, voiceChannels), false);
        embed.addField("Random Information", String.format("The bot can see %d emotes and %d roles.",
                emoteTotal, roleTotal), false);

        event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(
                Button.secondary(event.getAuthor().getId() + ":delete", "Delete")).queue();
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }
}
