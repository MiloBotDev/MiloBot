package commands.games.uno;

import commands.Command;
import commands.ParentCmd;
import commands.games.GamesCmd;
import games.uno.UnoGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class UnoCmd extends Command implements GamesCmd, ParentCmd {

    public UnoCmd() {
        this.commandName = "uno";
        this.commandDescription = "Play Uno with your friends!";
        this.subCommands.add(new UnoHostCmd());
        this.subCommands.add(new UnoInfoCmd());
//        this.subCommands.add(new UnoBotCmd());
        this.listeners.add(new ListenerAdapter() {
            @Override
            public void onMessageReceived(@NotNull MessageReceivedEvent event) {
                UnoGame.onMessageReceived(event);
            }
        });
        this.subCommands.forEach(subCmd -> subCmd.parentCommandName = this.commandName);
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
    }
}
