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
        eb.setTitle("Uno Information");
        String unoDescription = """
                UNO is a fast-paced and exciting card game that is played with a special deck of cards. 
                The goal of the game is to get rid of all of your cards before your opponents do.
                To start the game, each player is dealt seven cards, and the remaining cards are placed face down in a 
                draw pile. The top card of the draw pile is then turned over and placed next to the draw pile, 
                forming the discard pile. 
                
                On their turn, a player must play a card that matches the color, 
                number, or symbol (such as skip, reverse, or draw two) of the top card on the discard pile. 
                If a player does not have a matching card, they must draw a card from the draw pile and add it to 
                their hand. 
                
                There are several special cards that have special effects when played. For example, 
                the skip card causes the next player to lose their turn, the reverse card changes the direction of play, 
                and the draw two card forces the next player to draw two cards and lose their turn.
                
                The first player that gets rid of all of their cards wins the game.
                """;
        eb.setDescription(unoDescription);
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
