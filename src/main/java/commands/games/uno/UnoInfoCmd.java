package commands.games.uno;

import commands.Command;
import commands.SubCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.List;

public class UnoInfoCmd extends Command implements SubCmd {

    public UnoInfoCmd() {
        this.commandName = "info";
        this.commandDescription = "A simple tutorial on how to play uno with milobot.";
        this.aliases = new String[]{"i"};
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, List<String> args) {
        User author = event.getAuthor();
        event.getChannel().sendMessageEmbeds(getUnoInfoEmbed(author).build())
                .setActionRow(Button.secondary(author.getId() + ":delete", "Delete")).queue();
    }

    @Override
    public void executeSlashCommand(@NotNull SlashCommandEvent event) {
        User user = event.getUser();
        event.replyEmbeds(getUnoInfoEmbed(user).build())
               .addActionRow(Button.secondary(user.getId() + ":delete", "Delete")).queue();
    }

    public EmbedBuilder getUnoInfoEmbed(User user) {
        EmbedBuilder eb = new EmbedBuilder();
        EmbedUtils.styleEmbed(eb, user);

        return eb;
    }
}
