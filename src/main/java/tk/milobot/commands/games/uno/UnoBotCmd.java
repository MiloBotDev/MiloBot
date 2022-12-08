package tk.milobot.commands.games.uno;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.games.hungergames.model.LobbyEntry;
import tk.milobot.games.uno.UnoGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UnoBotCmd extends Command implements SubCmd {

    public UnoBotCmd() {
        this.commandName = "bot";
        this.commandDescription = "Let bots verse eachother in uno.";
        this.aliases = new String[]{"b"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        UnoGame unoGame = new UnoGame(List.of(new LobbyEntry("Bot 1"), new LobbyEntry("Bot 2")));
        unoGame.start(event.getChannel());
    }
}
