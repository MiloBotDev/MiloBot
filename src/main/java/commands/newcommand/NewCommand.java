package commands.newcommand;

import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

class NewCommand {


    public final void onCommand(@NotNull MessageReceivedEvent event) {
        if (this instanceof TextCommand) {
            ((TextCommand) this).executeCommand(event);
        }
    }

    public final void onCommand(@NotNull SlashCommandEvent event) {
        if (this instanceof SlashCommand) {
            ((SlashCommand) this).executeCommand(event);
        }
    }


}
