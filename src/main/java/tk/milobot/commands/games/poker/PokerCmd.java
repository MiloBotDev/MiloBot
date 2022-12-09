package tk.milobot.commands.games.poker;

import tk.milobot.commands.Command;
import tk.milobot.commands.ParentCmd;
import tk.milobot.commands.games.GamesCmd;
import tk.milobot.games.PokerGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class PokerCmd extends Command implements ParentCmd, GamesCmd {

    public PokerCmd() {
        this.commandName = "poker";
        this.commandDescription = "5-card Poker brought to discord.";
        this.subCommands.add(new PokerPlayCmd());
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.listeners.add(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                PokerGame.onMessageReceived(event);
            }
        });
        this.subCommands.add(new PokerPlayCmd());
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
    }

}
