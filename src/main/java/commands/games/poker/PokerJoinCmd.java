package commands.games.poker;

import commands.Command;
import commands.SubCmd;
import games.Poker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PokerJoinCmd extends Command implements SubCmd {
    public PokerJoinCmd() {
        this.commandName = "join";
        this.commandDescription = "Join a game of poker on discord.";
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        Poker game = Poker.getGameByChannel(event.getTextChannel());
        if (game == null) {
            event.getChannel().sendMessage("There is no game of poker in this channel.").queue();
        } else if (game.containsPlayer(event.getAuthor())) {
            event.getChannel().sendMessage("<@" + event.getAuthor().getId() + "> You are already in this game of " +
                    "poker.").queue();
        } else {
            game.addPlayer(event.getAuthor());
            event.getChannel().sendMessage("<@" + event.getAuthor().getId() + "> You have joined the game of " +
                    "poker.").queue();
        }
    }
}
