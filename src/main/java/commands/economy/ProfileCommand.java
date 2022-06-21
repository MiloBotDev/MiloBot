package commands.economy;

import commands.Command;
import database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import user.User;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The Profile Command.
 * Shows the user their own profile or that of someone else.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class ProfileCommand extends Command implements EconomyCommand {

    private final DatabaseManager manager;
    private final User user;

    public ProfileCommand() {
        this.commandName = "profile";
        this.commandDescription = "View your own or someone else's profile.";
        this.commandArgs = new String[]{"*user"};
        this.cooldown = 60;
        this.manager = DatabaseManager.getInstance();
        this.user = User.getInstance();
    }

    @Override
    public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
        if(args.size() == 0) {
            event.getChannel().sendTyping().queue();
            String userId = event.getAuthor().getId();
            String name = event.getAuthor().getName();
            makeEmbed(event, userId, name);
        } else {
            String findUser = String.join(" ", args);
            try {
                List<Member>  usersByName = new ArrayList<>();
                event.getGuild().findMembers(e -> e.getUser().getName().toLowerCase(Locale.ROOT).equals(findUser))
                        .onSuccess(members -> {
                           usersByName.addAll(members);
                            if (usersByName.size() == 0) {
                                event.getChannel().sendTyping().queue();
                                event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
                            } else {
                                event.getChannel().sendTyping().queue();
                                String userId = usersByName.get(0).getId();
                                String name = usersByName.get(0).getUser().getName();
                                makeEmbed(event, userId, name);
                            }
                        });
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constructs and sends the embed for the Profile command.
     * @param event - MessageReceivedEvent
     * @param userId - The id of the User you want to get the profile of
     * @param name - The name of the User you want to get the profile of
     */
    private void makeEmbed(MessageReceivedEvent event, String userId, String name) {
        EmbedBuilder embed = new EmbedBuilder();
        EmbedUtils.styleEmbed(event, embed);
        embed.setTitle(name);

        ArrayList<String> resultSelectUser = manager.query(manager.selectUser, DatabaseManager.QueryTypes.RETURN, userId);
        ArrayList<String> resultGetUserRank = manager.query(manager.getUserRankByExperience, DatabaseManager.QueryTypes.RETURN, userId);
        ArrayList<String> resultGetUserAmount = manager.query(manager.getUserAmount, DatabaseManager.QueryTypes.RETURN);
        if(resultSelectUser.size() == 0) {
            event.getChannel().sendMessage("User doesn't have a profile.").queue();
        }
        String userName = resultSelectUser.get(1);
        String currency = resultSelectUser.get(2);
        String level = resultSelectUser.get(3);
        String experience = resultSelectUser.get(4);
        String rank = resultGetUserRank.get(0);
        String userAmount = resultGetUserAmount.get(0);

        StringBuilder levelDescription = new StringBuilder();
        levelDescription.append(String.format("`Level`: %s.\n", level));
        levelDescription.append(String.format("`Experience`: %s.\n", experience));
        embed.addField("Level", levelDescription.toString(), false);
        generateLevelProgressBar(Integer.parseInt(level), Integer.parseInt(experience));

//        StringBuilder description = new StringBuilder();

//        description.append(String.format("`Wallet:` %s.\n", currency));
//        description.append(String.format("Your are ranked as number %s out of %s users.", rank, userAmount));
//        embed.setDescription(description);

        event.getChannel().sendMessageEmbeds(embed.build()).queue();
    }

    private String generateLevelProgressBar(int currentLevel, int currentExperience) {
        int nextLevel = currentLevel + 1;
        if(nextLevel > user.maxLevel) {
            return "You are at the maximum level.";
        }
        int nextlevelExperience = user.levels.get(nextLevel);
        int experienceDifference = nextlevelExperience - user.levels.get(currentLevel);
        int gainedExperience = nextlevelExperience - currentExperience;
        System.out.println(experienceDifference/gainedExperience);

        return "";
    }
}
