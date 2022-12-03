package commands.newcommand.extensions;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.Set;

public interface DefaultFlags extends Flags {

    @Override
    default Set<String> getFlags() {
        return Set.of("--help", "--stats");
    }

    @Override
    default void executeFlag(MessageReceivedEvent event, String flag) {
        // checks if --help flag is present as an argument
        if (flag.equals("--help")) {
            generateHelp(event);

        }
        // checks if the --stats flag is present as an argument
        else if (flag.contains("--stats")) {
            generateStats(event);
        }
        else {
            throw new IllegalArgumentException("Invalid flag: " + flag);
        }
    }
}
