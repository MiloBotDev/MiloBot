package commands.fun;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

/**
 * The user command.
 * Shows the user an overview of their own account details, or that of someone in the same server.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class UserCommand extends Command implements FunCommand {

    public UserCommand() {
        this.commandName = "user";
        this.commandDescription = "Shows information about a user.";
        this.commandArgs = new String[]{"*user"};
    }

    @Override
    public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        EmbedBuilder userEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(event, userEmbed);

        User author;
        User.Profile userProfile;
        Member member;

        if(args.size() == 0) {
            author = event.getAuthor();
            userProfile = author.retrieveProfile().complete();
        } else {
            List<Member> usersByName = event.getGuild().getMembersByName(args.get(0), true);
            if(usersByName.size() == 0) {
                event.getChannel().sendTyping().queue();
                event.getChannel().sendMessage(String.format("User `%s` not found.", args.get(0))).queue();
                return;
            } else {
                author = usersByName.get(0).getUser();
                userProfile = author.retrieveProfile().complete();
            }
        }

        member = Objects.requireNonNull(event.getGuild().getMemberById(author.getId()));

        userEmbed.setTitle(author.getName());
        userEmbed.setImage(userProfile.getBannerUrl());
        userEmbed.setThumbnail(author.getAvatarUrl());

        userEmbed.addField("Nickname", Objects.equals(member.getNickname(), null) ? "None" :
                member.getNickname(), false);
        userEmbed.addField("Account Created", author.getTimeCreated().format(dtf), false);
        userEmbed.addField("Joined Server", member.getTimeJoined().format(dtf), false);
        userEmbed.addField("Mutual Servers", "You guys are together in every server, " +
                "anything romantic going on?", false);

        StringBuilder roles = new StringBuilder();
        List<Role> memberRoles = member.getRoles();
        if(memberRoles.size() == 0) {
            roles.append("None");
        } else {
            for(int i = 0; i < memberRoles.size(); i++) {
                if(i + 1 == memberRoles.size()) {
                    roles.append(memberRoles.get(i).getName()).append(".");
                } else {
                    roles.append(memberRoles.get(i).getName()).append(", ");
                }
            }
        }
        userEmbed.addField("Roles", roles.toString(), false);

        event.getChannel().sendTyping().queue();
        event.getChannel().sendMessageEmbeds(userEmbed.build()).queue(EmbedUtils.deleteEmbedButton(event,
                event.getAuthor().getName()));
    }
}
