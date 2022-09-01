package commands.games.poker;

import commands.Command;
import commands.SubCmd;
import games.Poker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import utility.NewLobby;

import java.util.List;

public class PokerPlayCmd extends Command implements SubCmd {
    public PokerPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of poker on discord.";
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        new NewLobby("Poker lobby", event.getAuthor(),
                (players) -> {
                    Poker poker = new Poker(players);
                    poker.start();
                }, 2, 5).initialize(event.getChannel());
    }
}
