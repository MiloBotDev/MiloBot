package tk.milobot.commands.games.blackjack;

import tk.milobot.commands.Command;
import tk.milobot.commands.SubCmd;
import tk.milobot.games.BlackjackGame;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BlackjackPlayCmd extends Command implements SubCmd {

    public BlackjackPlayCmd() {
        this.commandName = "play";
        this.commandDescription = "Play a game of blackjack on discord.";
        this.commandArgs = new String[]{"bet*"};
        this.allowedChannelTypes.add(ChannelType.TEXT);
        this.allowedChannelTypes.add(ChannelType.PRIVATE);
        this.slashSubcommandData = new SubcommandData("play", "Play a game of blackjack on discord.")
                .addOptions(new OptionData(OptionType.INTEGER, "bet", "The amount of money you want to bet.", false)
                        .setRequiredRange(1, 10000));
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        BlackjackGame.newGame(event, args);
    }

    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.deferReply().queue();
        BlackjackGame.newGame(event);
    }
}
