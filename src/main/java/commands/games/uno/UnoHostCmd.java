package commands.games.uno;

import commands.Command;
import commands.SubCmd;
import games.uno.UnoGame;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import utility.lobby.Lobby;

import java.util.List;

public class UnoHostCmd extends Command implements SubCmd {

    public UnoHostCmd() {
        this.commandName = "host";
        this.commandDescription = "Host a game of Uno!";
        this.aliases =  new String[]{"h", "start"};
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        new Lobby("Uno Lobby", event.getAuthor(),
                (players, message) -> {
                    UnoGame unoGame = new UnoGame(players);
                    unoGame.start(event.getChannel());
                }, 2, 5).initialize(event.getChannel());
    }
}
