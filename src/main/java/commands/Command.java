package commands;

import database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Basic implementation of a command.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public abstract class Command {

    /**
     * The name of the command.
     */
    public String commandName = "commandName";

    /**
     * The description of the command.
     */
    public String commandDescription = "The description for this command";

    /**
     * The arguments the command has.
     */
    public String[] commandArgs = {};

    /**
     * The different flags the command can be flagged with.
     */
    public String[] flags = {"--help", "--stats"};

    /**
     * The aliases this command has.
     */
    public String[] aliases = {};

    /**
     * The time needed to call the command again in seconds.
     */
    public int cooldown = 0;

    /**
     * The permissions needed to use the command.
     */
    public HashMap<String, Permission> permissions = new HashMap<>();

    /**
     * A map that maps a users name to time when they can use the command again.
     */
    public HashMap<String, OffsetDateTime> cooldownMap = new HashMap<>();

    /**
     * The default constructor for a command.
     */
    public Command() {

    }

    /**
     * The default implementation for every command.
     * @param event - MessageReceivedEvent
     * @param args - The arguments provided as a String[]
     */
    public void execute(@NotNull MessageReceivedEvent event, List<String> args) {
        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessage("This command has not yet been implemented!").queue();
    }

    /**
     * The default implementation for checking if a flag is present.
     * @param event - MessageReceivedEvent
     * @param args - The arguments provided as a String[]
     * @param commandName - The name of the command
     * @param commandDescription - The description of the command
     * @param commandArgs - The arguments of the command
     * @param aliases - The different aliases for the command
     * @param flags - The flags for the command
     * @param cooldown - The cooldown for the command
     * @return true if a flag was present, false if no flag was present.
     */
    public boolean checkForFlags(MessageReceivedEvent event, @NotNull List<String> args, String commandName,
                                 String commandDescription, String[] commandArgs, String[] aliases, String[] flags,
                                 int cooldown) {
        boolean flagPresent = false;
        // checks if --help flag is present as an argument
        if(args.contains("--help")) {
            generateHelp(event, commandName, commandDescription, commandArgs, aliases, flags, cooldown);
            flagPresent = true;
        }
        // checks if the --stats flag is present as an argument
        if(args.contains("--stats")) {
            generateStats(event, commandName);
            flagPresent = true;
        }
        return flagPresent;
    }

    /**
     * Checks if the user is using the command again before the cooldown is over.
     * @param event - MessageReceivedEvent
     * @param cooldownMap - A HashMap containing the usernames and the times when they can use the command again.
     * @return true if the command is on cooldown, false if otherwise
     */
    public boolean checkCooldown(@NotNull MessageReceivedEvent event, @NotNull HashMap<String, OffsetDateTime> cooldownMap) {
        OffsetDateTime currentTime = event.getMessage().getTimeCreated();
        String authorId = event.getAuthor().getId();
        OffsetDateTime newAvailableTime = event.getMessage().getTimeCreated().plusSeconds(cooldown);
        if(cooldownMap.containsKey(authorId)) {
            OffsetDateTime availableTime = cooldownMap.get(authorId);
            if(currentTime.isBefore(availableTime)) {
                long waitTime = availableTime.toEpochSecond() - currentTime.toEpochSecond();
                event.getChannel().sendTyping().queue();
                event.getChannel().sendMessage(String.format("You can use this command again in %d seconds.", waitTime))
                        .queue();
                return true;
            } else {
                cooldownMap.replace(authorId, availableTime, newAvailableTime);
            }
        } else {
            cooldownMap.put(authorId, newAvailableTime);
        }
        return false;
    }

    /**
     * Updates the command tracker for the given command name.
     * @param commandName - The name of the command
     */
    public void updateCommandTracker(String commandName) {
        DatabaseManager manager = DatabaseManager.getInstance();
        ArrayList<String> query = manager.query(manager.checkIfCommandTracked, DatabaseManager.QueryTypes.RETURN, commandName);
        if(query.size() == 0) {
            manager.query(manager.addCommandToTracker, DatabaseManager.QueryTypes.UPDATE, commandName, "1");
        } else {
            ArrayList<String> result = manager.query(manager.checkCommandUsageAmount, DatabaseManager.QueryTypes.RETURN, commandName);
            String newAmount = Integer.toString(Integer.parseInt(result.get(0)) + 1);
            manager.query(manager.updateCommandUsageAmount, DatabaseManager.QueryTypes.UPDATE, newAmount, commandName);
        }
    }

    /**
     * Generates a message with the stats of that specific command.
     * @param event - MessageReceivedEvent
     * @param commandName - The name of the command
     */
    public void generateStats(@NotNull MessageReceivedEvent event, String commandName) {
        DatabaseManager manager = DatabaseManager.getInstance();
        ArrayList<String> amount = manager.query(manager.checkCommandUsageAmount, DatabaseManager.QueryTypes.RETURN, commandName);

        EmbedBuilder stats = new EmbedBuilder();
        EmbedUtils.styleEmbed(event, stats);
        stats.setTitle(String.format("Stats for %s", commandName));
        stats.addField("Usages", String.format("This command has been used %d times.", Integer.parseInt(amount.get(0))), false);

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessageEmbeds(stats.build()).queue(EmbedUtils.deleteEmbedButton(event, event.getAuthor().getName()));
    }

    /**
     * Generates a standard help message for when the command is called with the --help flag.
     * @param event - MessageReceivedEvent
     * @param commandName - The name of the command
     * @param commandDescription - The description of the command
     * @param commandArgs - The arguments of the command
     * @param aliases - The different aliases for the command
     * @param flags - The flags for the command
     * @param cooldown - The cooldown for the command
     */
    public void generateHelp(@NotNull MessageReceivedEvent event, String commandName, String commandDescription,
                             String @NotNull [] commandArgs, String @NotNull [] aliases, String[] flags, int cooldown) {
        String consumerName = event.getAuthor().getName();

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(event, info);
        info.setTitle(commandName);
        info.setDescription(commandDescription);

        StringBuilder argumentsText = getArgumentsText(commandName, commandArgs);
        info.addField(
                "Usage",
                argumentsText.toString(),
                false
        );

        if(!(aliases.length == 0)) {
            StringBuilder aliasesText = new StringBuilder();
            for(int i = 0; i < aliases.length; i++) {
                aliasesText.append('`').append(aliases[i]).append('`');
                if(!(i + 1 == flags.length)) {
                    aliasesText.append(", ");
                }
            }
            info.addField("Aliases", aliasesText.toString(),false);
        }

        if(!(flags.length == 0)) {
            StringBuilder flagsText = new StringBuilder();
            for(int i = 0; i < flags.length; i++) {
                flagsText.append('`').append(flags[i]).append('`');
                if(!(i + 1 == flags.length)) {
                    flagsText.append(", ");
                }
            }
            info.addField("Flags", flagsText.toString(),false);
        }

        if(cooldown > 0) {
            info.addField("Cooldown", String.format("%d seconds.", cooldown), false);
        }

        if(!permissions.isEmpty()) {
            StringBuilder permissionsText = new StringBuilder();
            permissions.forEach((s, permission) -> permissionsText.append("`").append(s).append("`"));
            info.addField("Permissions", permissionsText.toString(), false);
        }

        event.getChannel().sendTyping().queue();
        MessageAction messageAction = event.getChannel().sendMessageEmbeds(info.build());
        messageAction.queue(EmbedUtils.deleteEmbedButton(event, consumerName));
    }

    /**
     * Builds a String that explains the usage of a command.
     * @param commandName - The name of the command
     * @param commandArgs - The arguments of the command
     * @return the String as a StringBuilder instance
     */
    @NotNull
    private StringBuilder getArgumentsText(String commandName, String @NotNull [] commandArgs) {
        StringBuilder argumentsText = new StringBuilder();
        if(commandArgs.length == 0) {
            argumentsText.append("`!").append(commandName).append("`");
        } else {
            argumentsText.append("`!").append(commandName).append(" ");
            for(int i = 0; i < commandArgs.length; i++) {
                argumentsText.append("{").append(commandArgs[i]).append("}");
                if(!(i + 1 == commandArgs.length)) {
                    argumentsText.append(" ");
                }
            }
            argumentsText.append('`');
            argumentsText.append("\n Arguments marked with * are optional, " +
                    "arguments marked with ** accept multiple inputs.");
        }
        return argumentsText;
    }

    /**
     * Generates a message for when the command has been improperly used.
     * @param event - MessageReceivedEvent
     * @param commandName - The name of the command
     * @param commandArgs - The arguments of the command
     */
    public void generateCommandUsage(@NotNull MessageReceivedEvent event, String commandName, String @NotNull [] commandArgs) {
        String consumerName = event.getAuthor().getName();

        EmbedBuilder info = new EmbedBuilder();
        EmbedUtils.styleEmbed(event, info);
        info.setTitle("Missing required arguments");


        info.setDescription(getArgumentsText(commandName, commandArgs));

        event.getChannel().sendTyping().queue();
        MessageAction messageAction = event.getChannel().sendMessageEmbeds(info.build());
        messageAction.queue(EmbedUtils.deleteEmbedButton(event, consumerName));
    }

    /**
     * Calculates the required amount of arguments a command has.
     * @param commandArgs - All the command arguments
     * @return the required amount of arguments
     */
    public int calculateRequiredArgs(String @NotNull [] commandArgs) {
        int requiredArgs = 0;
        for (String commandArg : commandArgs) {
            if (!commandArg.contains("*")) {
                requiredArgs += 1;
            }
        }
        return requiredArgs;
    }

}