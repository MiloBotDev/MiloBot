package commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.restaction.MessageAction;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    public String[] flags = {"--help"};

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
     * @return true if a flag was present, false if no flag was present.
     */
    public boolean checkForFlags(MessageReceivedEvent event, @NotNull List<String> args, String commandName,
                                 String commandDescription, String[] commandArgs, String[] flags) {
        // checks if --help flag is present as an argument
        if(args.contains("--help")) {
            generateHelp(event, commandName, commandDescription, commandArgs, flags);
            return true;
        }
        return false;
    }

    /**
     * Generates a standard help message for when the command is called with the --help flag.
     * @param event - - MessageReceivedEvent
     */
    public void generateHelp(@NotNull MessageReceivedEvent event, String COMMAND_NAME, String COMMAND_DESCRIPTION,
                             String @NotNull [] COMMAND_ARGS, String[] FLAGS) {
        String consumerName = event.getAuthor().getName();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

        EmbedBuilder info = new EmbedBuilder();
        info.setColor(Color.BLUE);
        info.setTitle(COMMAND_NAME);
        info.setAuthor(event.getAuthor().getName());
        info.setDescription(COMMAND_DESCRIPTION);

        StringBuilder argumentsText = new StringBuilder();
        if(COMMAND_ARGS.length == 0) {
            argumentsText.append("`!").append(COMMAND_NAME).append("`");
        } else {
            argumentsText.append("`!").append(COMMAND_NAME).append(" ");
            for(int i = 0; i < COMMAND_ARGS.length; i++) {
                argumentsText.append("{").append(COMMAND_ARGS[i]).append("}");
                if(!(i + 1 == COMMAND_ARGS.length)) {
                    argumentsText.append(" ");
                }
            }
            argumentsText.append('`');
            argumentsText.append("\n Arguments marked with * are optional, " +
                    "arguments marked with ** accept multiple inputs.");
        }
        info.addField(
                "Usage",
                argumentsText.toString(),
                false
        );

        StringBuilder flagsText = new StringBuilder();
        if(FLAGS.length == 0) {
            flagsText.append("None.");
        } else {
            for(int i = 0; i < FLAGS.length; i++) {
                flagsText.append('`').append(FLAGS[i]).append('`');
                if(!(i + 1 == FLAGS.length)) {
                    argumentsText.append(", ");
                }
            }
        }
        info.addField(
                "Flags",
                flagsText.toString(),
                false
                );

        info.setFooter(dtf.format(LocalDateTime.now()));

        event.getChannel().sendTyping().queue();
        MessageAction messageAction = event.getChannel().sendMessageEmbeds(info.build());
        messageAction.queue((message) ->  {
            message.addReaction("❌").queue();
            ListenerAdapter listener = new ListenerAdapter() {
                @Override
                public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
                    String messageId = event.getMessageId();
                    if (Objects.requireNonNull(event.getUser()).getName().equals(consumerName) &&
                            event.getReactionEmote().getAsReactionCode().equals("❌") && message.getId().equals(messageId)) {
                        event.getChannel().deleteMessageById(messageId).queue();
                        event.getJDA().removeEventListener(this);
                    }
                }
            };
            message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener), 1, TimeUnit.MINUTES);
            message.getJDA().addEventListener(listener);
        });
    }

}