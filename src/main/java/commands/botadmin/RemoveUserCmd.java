package commands.botadmin;

import commands.Command;
import models.CustomEmojis;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveUserCmd extends Command {

    public RemoveUserCmd() {
        this.commandName = "remove";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
//        DatabaseManager.getInstance().query(UserTableQueries.removeUser, DatabaseManager.QueryTypes.UPDATE, args.get(0));
        event.getChannel().sendMessage(String.format("%s", CustomEmojis.UNO_BLUE_SIX.getEmoji())).queue();
    }
}
