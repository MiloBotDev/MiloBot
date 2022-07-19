package commands.economy;

import commands.Command;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DailyCmd extends Command implements EconomyCmd {

    public DailyCmd() {
        this.commandName = "daily";
        this.commandDescription = "Collect your daily reward.";
    }

    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
    }

    public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
    }

}
