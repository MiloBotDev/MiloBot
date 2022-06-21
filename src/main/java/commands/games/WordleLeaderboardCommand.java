package commands.games;

import commands.Command;
import commands.SubCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class WordleLeaderboardCommand extends Command implements SubCommand {

    public WordleLeaderboardCommand() {
        this.commandName = "leaderboard";
        this.commandDescription = "Check the wordle leaderboards.";
    }

    @Override
    public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        event.getChannel().sendMessage("test").queue();
    }

}
