package commands.games.poker;

import commands.Command;
import commands.SubCmd;
import games.Poker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PokerStartCmd extends Command implements SubCmd {
    public PokerStartCmd() {
        this.commandName = "start";
        this.commandDescription = "Start a game of poker on discord.";
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        Poker game = Poker.getGameByChannel(event.getTextChannel());
        if (game == null) {
            event.getChannel().sendMessage("There is no game of poker in this channel.").queue();
        } else if (game.getMasterUser() == event.getAuthor()) {
            game.start();
        } else {
            event.getChannel().sendMessage("You must be the creator of this game of poker to start the game.").queue();
        }
    }
}
