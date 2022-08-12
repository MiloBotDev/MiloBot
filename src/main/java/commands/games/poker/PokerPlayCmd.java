package commands.games.poker;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.Lobby;

import java.util.List;

public class PokerPlayCmd extends Command implements SubCmd {
    public PokerPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of poker on discord.";
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        Lobby lobby = new Lobby(event.getAuthor().getId(), event.getAuthor().getAsMention());
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Poker Lobby");
        embed.setDescription(lobby.generateDescription());
        event.getChannel()
                .sendMessageEmbeds(embed.build())
                .setActionRows(ActionRow.of(
                        Button.primary(event.getAuthor().getId() + ":joinLobby", "Join"),
                        Button.primary(event.getAuthor().getId() + ":leaveLobby", "Leave"),
                        Button.primary(event.getAuthor().getId() + ":start_poker", "Start"),
                        Button.secondary(event.getAuthor().getId() + ":delete", "Delete")
                ))
                .queue(message -> {
                    String messageId = message.getId();
                    lobby.initialize(messageId);
                });
    }
}
