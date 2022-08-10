package commands.games.poker;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;
import games.Poker;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PokerCmd extends Command implements ParentCmd, GamesCmd {

    public PokerCmd() {
        this.commandName = "poker";
        this.commandDescription = "5-card Poker brought to discord.";
        this.subCommands.add(new PokerPlayCmd());
        this.subCommands.add(new PokerJoinCmd());
        this.subCommands.add(new PokerStartCmd());
        this.listeners.add(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                Poker.onMessageReceived(event);
            }
        });
    }

}
