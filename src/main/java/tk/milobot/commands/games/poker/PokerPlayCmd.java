package tk.milobot.commands.games.poker;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.games.PokerGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import tk.milobot.utility.lobby.Lobby;

import java.util.List;

public class PokerPlayCmd extends Command implements SubCmd {

    public PokerPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of poker on discord.";
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        new Lobby("Poker lobby", event.getAuthor(),
                (players, message) -> {
                    PokerGame pokerGame = new PokerGame(players);
                    pokerGame.start();
                }, 2, 5).initialize(event.getChannel());
    }
}
