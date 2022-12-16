package tk.milobot.commands.utility;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.BaseCommand;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import org.jetbrains.annotations.NotNull;
import tk.milobot.commands.command.ParentCommand;
import tk.milobot.commands.command.extensions.*;
import tk.milobot.utility.EmbedUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * Shows the user an overview of their own account details, or that of someone in the same server.
 */
public class UserCmd extends ParentCommand implements TextCommand, SlashCommand, DefaultFlags,
        DefaultChannelTypes, DefaultCommandArgs, UtilityCmd {

    private final ExecutorService executorService;

    public UserCmd(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public @NotNull BaseCommand<?> getCommandData() {
        return new CommandData("user", "Shows information on the user you are using this command on.")
                .addOptions(new OptionData(OptionType.USER, "user", "The user to show information on.")
                        .setRequired(false));
    }

    @Override
    public List<String> getCommandArgs() {
        return List.of("user");
    }

    @Override
    public boolean checkRequiredArgs(MessageReceivedEvent event, List<String> args) {
        return true;
    }

    @Override
    public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        EmbedBuilder userEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(userEmbed, event.getAuthor());

        final User[] user = new User[1];
        final User.Profile[] userProfile = new User.Profile[1];
        final Member[] member = new Member[1];

        if (args.size() == 0) {
            user[0] = event.getAuthor();
            userProfile[0] = user[0].retrieveProfile().complete();
            member[0] = event.getMember();
            user[0].retrieveProfile().queue(profile -> {
                userProfile[0] = profile;
                EmbedBuilder embedBuilder = makeEmbed(dtf, userEmbed, user[0], userProfile[0], member[0]);
                event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
            });
        } else {
            String findUser = String.join(" ", args);
            try {
                List<Member> usersByName = new ArrayList<>();
                event.getGuild().findMembers(e -> e.getUser().getName().toLowerCase(Locale.ROOT).equals(findUser))
                        .onSuccess(members -> {
                            usersByName.addAll(members);
                            if (usersByName.size() == 0) {
                                event.getChannel().sendTyping().queue();
                                event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
                            } else {
                                event.getChannel().sendTyping().queue();
                                user[0] = usersByName.get(0).getUser();
                                member[0] = usersByName.get(0);
                                user[0].retrieveProfile().queue(profile -> {
                                    userProfile[0] = profile;
                                    EmbedBuilder embedBuilder = makeEmbed(dtf, userEmbed, user[0], userProfile[0], member[0]);
                                    event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
                                });
                            }
                        });
            } catch (IllegalStateException e) {
                event.getChannel().sendTyping().queue();
                event.getChannel().sendMessage("You can only look up other users in a server.").queue();
            }
        }
    }

    @Override
    public void executeCommand(SlashCommandEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        EmbedBuilder userEmbed = new EmbedBuilder();
        EmbedUtils.styleEmbed(userEmbed, event.getUser());

        final User[] user = new User[1];
        final User.Profile[] userProfile = new User.Profile[1];
        final Member[] member = new Member[1];

        if (event.getOption("user") == null) {
            user[0] = event.getUser();
            userProfile[0] = user[0].retrieveProfile().complete();
            member[0] = event.getMember();
            user[0].retrieveProfile().queue(profile -> {
                userProfile[0] = profile;
                EmbedBuilder embedBuilder = makeEmbed(dtf, userEmbed, user[0], userProfile[0], member[0]);
                event.replyEmbeds(embedBuilder.build()).queue();
            });
        } else {
            try {
                user[0] = event.getOption("user").getAsUser();
                member[0] = event.getGuild().getMember(user[0]);
                user[0].retrieveProfile().queue(profile -> {
                    userProfile[0] = profile;
                    EmbedBuilder embedBuilder = makeEmbed(dtf, userEmbed, user[0], userProfile[0], member[0]);
                    event.replyEmbeds(embedBuilder.build()).queue();
                });
            } catch (NullPointerException e) {
                event.reply("User not found.").queue();
            }

        }
    }

    /**
     * Constructs and sends the embed for the User command.
     */
    private EmbedBuilder makeEmbed(DateTimeFormatter dtf, @NotNull EmbedBuilder userEmbed, @NotNull User user,
                                   User.@NotNull Profile profile, Member member) {
        userEmbed.setTitle(user.getName());
        userEmbed.setImage(profile.getBannerUrl());
        userEmbed.setThumbnail(user.getAvatarUrl());

        if (member != null) {
            userEmbed.addField("Nickname", Objects.equals(member.getNickname(), null) ? "None" :
                    member.getNickname(), false);
            userEmbed.addField("Joined Server", member.getTimeJoined().format(dtf), false);
            StringBuilder roles = new StringBuilder();
            List<Role> memberRoles = member.getRoles();
            if (memberRoles.size() == 0) {
                roles.append("None.");
            } else {
                for (int i = 0; i < memberRoles.size(); i++) {
                    if (i + 1 == memberRoles.size()) {
                        roles.append("`").append(memberRoles.get(i).getName()).append("`").append(".");
                    } else {
                        roles.append("`").append(memberRoles.get(i).getName()).append("`").append(", ");
                    }
                }
            }
            userEmbed.addField("Roles", roles.toString(), false);
        }

        userEmbed.addField("Account Created", user.getTimeCreated().format(dtf), false);

        return userEmbed;
    }

    @Override
    public @NotNull Set<ChannelType> getAllowedChannelTypes() {
        return Set.of(ChannelType.TEXT);
    }

    @Override
    public @NotNull ExecutorService getExecutorService() {
        return this.executorService;
    }
}
