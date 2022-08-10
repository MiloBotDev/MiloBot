package commands.games.poker;

import commands.Command;
import commands.SubCmd;
import games.Poker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PokerPlayCmd extends Command implements SubCmd {
    public PokerPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of poker on discord.";
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if (Poker.getGameByChannel(event.getTextChannel()) == null) {
            new Poker(event.getAuthor(), event.getTextChannel());
            event.getChannel().sendMessage("New poker game issued.").queue();
        } else {
            event.getChannel().sendMessage("A game of poker is already in progress in this channel.").queue();
        }
    }
}
