package io.github.milobotdev.milobot.commands.games.uno;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class UnoInfoCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs {

    private final ExecutorService executorService;

    public UnoInfoCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("info", "A simple tutorial on how to play uno with milobot.");
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        User author = event.getAuthor();
        event.getChannel().sendMessageEmbeds(getUnoInfoEmbed(author).build())
                .setActionRow(Button.secondary(author.getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        User user = event.getUser();
        event.replyEmbeds(getUnoInfoEmbed(user).build())
               .addActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
    }

    public EmbedBuilder getUnoInfoEmbed(User user) {
        EmbedBuilder eb = new EmbedBuilder();
        EmbedUtils.styleEmbed(eb, user);

        return eb;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }
}
