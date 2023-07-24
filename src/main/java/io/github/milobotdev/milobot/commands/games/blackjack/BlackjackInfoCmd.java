package io.github.milobotdev.milobot.commands.games.blackjack;

import io.github.milobotdev.milobot.commands.command.SubCommand;
import io.github.milobotdev.milobot.commands.command.extensions.*;
import io.github.milobotdev.milobot.utility.EmbedUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class BlackjackInfoCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs {

    private final ExecutorService executorService;

    public BlackjackInfoCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void executeCommand(SlashCommandInteractionEvent event) {
        User user = event.getUser();
        event.replyEmbeds(createBlackjackInfoEmbed(user).build())
                .addActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        User author = event.getAuthor();
        event.getChannel().sendMessageEmbeds(createBlackjackInfoEmbed(author).build())
                .setActionRow(Button.secondary(author.getId() + ":delete", "Delete")).queue();
    }

    @Override
    public @NotNull CommandData getCommandData() {
        return new SubcommandData("info", "A simple tutorial on the rules of blackjack.");
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return DefaultChannelTypes.super.getAllowedChannelTypes();
    }

    private @NotNull EmbedBuilder createBlackjackInfoEmbed(User user) {
        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(info, user);
        info.setTitle("Blackjack Information");
        info.addField("The Objective", "The objective is to beat the dealer by getting as close to 21 as " +
                "possible without going over it.", false);
        info.addField("Scoring", "Each card from 1-10 is worth its own number, an ace is either 1 or 11 and " +
                "face cards are worth 10 points (Jack, King etc..)", false);
        info.addField("Gameplay", "The dealer (Milobot) shuffles the deck and hands you two cards, and itself " +
                "1 card, you then have the option to stand and wait out what the dealer does or to draw another card (hit). " +
                "If you draw another card and your total gets over 21 you lose. For the dealer its simple, if the total " +
                "is 16 or under it draws another card, otherwise it stands. If you happen to get 21 points with your " +
                "first 2 cards you win right away, this is called a blackjack.",false);
        info.addField("Betting", "Betting when playing with Milobot is completely optional and can only be" +
                "done with our own currency: morbcoins. If you bet 500 morbcoins they will be taken from your " +
                "account as soon as the game starts, if you manage to win you will win 100% of your bet back, and " +
                "in the case of a blackjack you will win 150% of your bet back.", false);
        return info;
    }
}