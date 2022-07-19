package commands.botadmin;

import commands.Command;
import database.DatabaseManager;
import database.queries.UserTableQueries;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RemoveUserCmd extends Command {

    public RemoveUserCmd() {
        this.commandName = "remove";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        DatabaseManager.getInstance().query(UserTableQueries.removeUser, DatabaseManager.QueryTypes.UPDATE, args.get(0));
        event.getChannel().sendMessage(args.get(0)).queue();
    }
}
