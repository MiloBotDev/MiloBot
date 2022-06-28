package commands.economy;

import commands.Command;
import database.DatabaseManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;
import utility.User;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Shows the user their own profile or that of someone else.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class ProfileCmd extends Command implements EconomyCmd {

	private final DatabaseManager manager;
	private final User user;

	public ProfileCmd() {
		this.commandName = "profile";
		this.commandDescription = "View your own or someone else's profile.";
		this.commandArgs = new String[]{"*user"};
		this.cooldown = 0;
		this.manager = DatabaseManager.getInstance();
		this.user = User.getInstance();
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if (args.size() == 0) {
			event.getChannel().sendTyping().queue();
			String userId = event.getAuthor().getId();
			String name = event.getAuthor().getName();
			makeEmbed(event, userId, name);
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
	 *
	 * @param event  - MessageReceivedEvent
	 * @param userId - The id of the User you want to get the profile of
	 * @param name   - The name of the User you want to get the profile of
	 */
	private void makeEmbed(MessageReceivedEvent event, String userId, String name) {
		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(event, embed);
		embed.setTitle(name);

		ArrayList<String> resultSelectUser = manager.query(manager.selectUser, DatabaseManager.QueryTypes.RETURN, userId);
		ArrayList<String> resultGetUserRank = manager.query(manager.getUserRankByExperience, DatabaseManager.QueryTypes.RETURN, userId);
		ArrayList<String> resultGetUserAmount = manager.query(manager.getUserAmount, DatabaseManager.QueryTypes.RETURN);
		if (resultSelectUser.size() == 0) {
			event.getChannel().sendMessage("User doesn't have a profile.").queue();
		}
		String userName = resultSelectUser.get(1);
		String currency = resultSelectUser.get(2);
		String level = resultSelectUser.get(3);
		String experience = resultSelectUser.get(4);
		String rank = resultGetUserRank.get(0);
		String userAmount = resultGetUserAmount.get(0);

		StringBuilder levelDescription = new StringBuilder();
		String levelProgressBar = generateLevelProgressBar(Integer.parseInt(level), Integer.parseInt(experience));
		levelDescription.append(String.format("**Level:** `%s`\n", level));
		levelDescription.append(String.format("**Experience:** `%s`\n", experience));
		levelDescription.append("**Progress till next level:** ");
		levelDescription.append(String.format("`%s`", levelProgressBar));
		embed.addField("Level", levelDescription.toString(), false);

		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}

	private @NotNull String generateLevelProgressBar(int currentLevel, int currentExperience) {
		int nextLevel = currentLevel + 1;
		if (nextLevel > user.maxLevel) {
			return "You are at the maximum level.";
		}

		int experienceDifference = user.levels.get(nextLevel) - user.levels.get(currentLevel);
		int gainedExperience = currentExperience - user.levels.get(currentLevel);
		float percentageDone = (float) gainedExperience / experienceDifference;

		DecimalFormat df = new DecimalFormat("#.#");
		String neededBlocksFormat = df.format(percentageDone);
		int neededBlocks = Integer.parseInt(neededBlocksFormat.substring(neededBlocksFormat.length() - 1));

		df = new DecimalFormat("#.##");
		String percentageDoneFormat = df.format(percentageDone);
		if(percentageDoneFormat.length() == 3) {
			percentageDoneFormat += "0";
		}
		String percentageDoneString;
		if(percentageDoneFormat.equals("0")) {
			percentageDoneString = "0";
		} else {
			percentageDoneString = percentageDoneFormat.substring(percentageDoneFormat.length() - 2);
		}

		StringBuilder progressBar = new StringBuilder("[");
		for(int i =0; i < 10; i++) {
			if(neededBlocks > 0) {
				progressBar.append("#");
				neededBlocks--;
			} else {
				progressBar.append("-");
			}
		}
		progressBar.append(String.format("] %s", percentageDoneString)).append("%");

		return progressBar.toString();
	}
}
