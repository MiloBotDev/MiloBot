package tk.milobot.commands.games.uno;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.games.hungergames.model.LobbyEntry;
import tk.milobot.games.uno.UnoGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.milobot.utility.lobby.BotLobby;

import java.util.ArrayList;
import java.util.List;

public class UnoHostCmd extends Command implements SubCmd {

    private static final Logger logger = LoggerFactory.getLogger(UnoHostCmd.class);

    public UnoHostCmd() {
        this.commandName = "host";
        this.commandDescription = "Host a game of Uno!";
        this.aliases =  new String[]{"start", "play", "h"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        int maxPlayers = 4;
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
                }, 2, 4);
        if(args.size() > 0) {
            try {
                int i = Integer.parseInt(args.get(0));
                if(i < 2 || i > 4) {
                    channel.sendMessage("maxPlayers must be a number between 2 and 4.").queue();
                } else {
                    unoLobby.setMaxPlayers(i);
                    unoLobby.initialize(channel);
                }
            } catch (NumberFormatException e) {
                logger.error("Failed formatting argument to number when setting the max players for uno lobby", e);
                channel.sendMessage("maxPlayers must be a number between 2 and 4.").queue();
            }
        } else {
            unoLobby.setMaxPlayers(maxPlayers);
            unoLobby.initialize(channel);
        }
    }
}
