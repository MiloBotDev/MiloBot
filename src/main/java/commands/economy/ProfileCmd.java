package commands.economy;

import commands.Command;
import database.DatabaseManager;
import database.queries.UserTableQueries;
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
import java.util.Optional;

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
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
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
								event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
							} else {
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
		ArrayList<String> resultSelectUser = manager.query(UserTableQueries.selectUser, DatabaseManager.QueryTypes.RETURN, userId);
		ArrayList<String> resultGetUserRank = manager.query(UserTableQueries.getUserRankByExperience, DatabaseManager.QueryTypes.RETURN, userId);
		ArrayList<String> resultGetUserAmount = manager.query(UserTableQueries.getUserAmount, DatabaseManager.QueryTypes.RETURN);
		String userName = resultSelectUser.get(1);
		String currency = resultSelectUser.get(2);
		String level = resultSelectUser.get(3);
		String experience = resultSelectUser.get(4);
		String rank = resultGetUserRank.get(0);
		String userAmount = resultGetUserAmount.get(0);

		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, event.getAuthor());
		embed.setTitle(name);

		if (resultSelectUser.size() == 0) {
			event.getChannel().sendMessage("User doesn't have a profile.").queue();
		}

		StringBuilder levelDescription = new StringBuilder();
		Optional<String> levelProgressBar = generateLevelProgressBar(Integer.parseInt(level), Integer.parseInt(experience));
		levelDescription.append(String.format("**Level:** `%s`\n", level));
		levelDescription.append(String.format("**Experience:** `%s`\n", experience));
		if (levelProgressBar.isPresent()) {
			levelDescription.append("**Progress till next level:** ");
			levelDescription.append(String.format("`%s`\n", levelProgressBar));
		} else {
			levelDescription.append("You are at the maximum level.\n");
		}
		levelDescription.append(String.format("You are rank `%s` out of `%s` people.", rank, userAmount));
		embed.setDescription(levelDescription.toString());

		event.getChannel().sendMessageEmbeds(embed.build()).queue();
	}

	private @NotNull Optional<String> generateLevelProgressBar(int currentLevel, int currentExperience) {
		int nextLevel = currentLevel + 1;
		if (nextLevel > user.maxLevel) {
			return Optional.empty();
		}

		int experienceDifference = user.levels.get(nextLevel) - user.levels.get(currentLevel);
		int gainedExperience = currentExperience - user.levels.get(currentLevel);
		float percentageDone = (float) gainedExperience / experienceDifference;

		DecimalFormat df = new DecimalFormat("#.#");
		String neededBlocksFormat = df.format(percentageDone);
		int neededBlocks = Integer.parseInt(neededBlocksFormat.substring(neededBlocksFormat.length() - 1));

		df = new DecimalFormat("#.##");
		String percentageDoneFormat = df.format(percentageDone);
		if (percentageDoneFormat.length() == 3) {
			percentageDoneFormat += "0";
		}
		String percentageDoneString;
		if (percentageDoneFormat.equals("0")) {
			percentageDoneString = "0";
		} else {
			percentageDoneString = percentageDoneFormat.substring(percentageDoneFormat.length() - 2);
		}

		StringBuilder progressBar = new StringBuilder("[");
		for (int i = 0; i < 10; i++) {
			if (neededBlocks > 0) {
				progressBar.append("#");
				neededBlocks--;
			} else {
				progressBar.append("-");
			}
		}
		progressBar.append(String.format("] %s", percentageDoneString)).append("%");

		return Optional.of(progressBar.toString());
	}
}
