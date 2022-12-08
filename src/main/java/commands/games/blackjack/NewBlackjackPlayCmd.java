package commands.games.blackjack;

import commands.newcommand.SubCommand;
import commands.newcommand.extensions.DefaultFlags;
import commands.newcommand.extensions.SlashCommand;
import commands.newcommand.extensions.TextCommand;
import games.BlackjackGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

public class NewBlackjackPlayCmd extends SubCommand implements TextCommand, SlashCommand, DefaultFlags {
    private final ExecutorService executorService;

    public NewBlackjackPlayCmd(@NotNull ExecutorService executorService) {
        this.executorService = executorService;
        /*this.commandName = "play";
        this.commandDescription = "Play a game of blackjack on discord.";
        this.commandArgs = new String[]{"bet*"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData("play", "Play a game of blackjack on discord.")
                .addOptions(new OptionData(OptionType.INTEGER, "bet", "The amount of money you want to bet.", false)
                        .setRequiredRange(1, 10000));*/
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        BlackjackGame.newGame(event, args);
    }

    @Override
    public void executeCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        BlackjackGame.newGame(event);
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new SubcommandData("play", "Play a game of blackjack on discord.")
                .addOptions(new OptionData(OptionType.INTEGER, "bet", "The amount of money you want to bet.", false)
                        .setRequiredRange(1, 10000));
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public @NotNull List<String> getCommandArgs() {
        return List.of("bet*");
    }

    @Override
    public boolean checkRequiredArgs(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (args.size() < 1) {
            sendMissingArgs(event);
            return false;
        } else if (args.size() > 1) {
            sendTooManyArgs(event);
            return false;
        } else {
            try {
                int bet = Integer.parseInt(args.get(0));
                if (bet < 1 || bet > 10000) {
                    sendInvalidArgs(event, "The bet must be between 1 and 10000.");
                    return false;
                }
            } catch (NumberFormatException e) {
                sendInvalidArgs(event, "The bet must be a number.");
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.TEXT, ChannelType.PRIVATE);
    }
}
