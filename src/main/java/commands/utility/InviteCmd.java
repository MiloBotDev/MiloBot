package commands.utility;

import commands.Command;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * The invite command.
 * Sends the user an invite link, so they can invite the bot to their own server.
 */
public class InviteCmd extends Command implements UtilityCmd {

    private final String inviteUrl;

    public InviteCmd() {
        this.commandName = "invite";
        this.commandDescription = "Sends an invite link to add the bot to another server.";
        this.aliases = new String[]{"inv"};
        this.inviteUrl = "https://discord.com/api/oauth2/authorize?client_id=993881386618466314&permissions=8&scope=applications.commands%20bot";
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        event.getChannel().sendMessage(inviteUrl).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        event.reply(inviteUrl).queue();
    }
}

