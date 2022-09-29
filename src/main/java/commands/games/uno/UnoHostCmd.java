package commands.games.uno;

import commands.Command;
import commands.SubCmd;
import games.hungergames.models.LobbyEntry;
import games.uno.UnoGame;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.lobby.BotLobby;

import java.util.ArrayList;
import java.util.List;

public class UnoHostCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(UnoHostCmd.class);

    public UnoHostCmd() {
        this.commandName = "host";
        this.commandDescription = "Host a game of Uno!";
        this.aliases =  new String[]{"h", "start"};
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        int maxPlayers = 8;
        MessageChannel channel = event.getChannel();
        BotLobby unoLobby = new BotLobby("Uno Lobby", event.getAuthor(),
                (entries, message) -> {
                    ArrayList<LobbyEntry> participants = new ArrayList<>();
                    entries.forEach((players, npcs) -> {
                        npcs.forEach(npc -> participants.add(new LobbyEntry(npc.getName())));
                        players.forEach(user -> participants.add(new LobbyEntry(user)));
                    });
                    UnoGame unoGame = new UnoGame(participants);
                    unoGame.start(channel);
                }, 2, 5);
        if(args.size() > 0) {
            try {
                int i = Integer.parseInt(args.get(0));
                if(i < 2 || i > 8) {
                    channel.sendMessage("maxPlayers must be a number between 2 and 8.").queue();
                } else {
                    unoLobby.setMaxPlayers(i);
                    unoLobby.initialize(channel);
                }
            } catch (NumberFormatException e) {
                logger.error("Failed formatting argument to number when setting the max players for uno lobby", e);
                channel.sendMessage("maxPlayers must be a number between 2 and 8.").queue();
            }
        } else {
            unoLobby.setMaxPlayers(maxPlayers);
            unoLobby.initialize(channel);
        }
    }
}
