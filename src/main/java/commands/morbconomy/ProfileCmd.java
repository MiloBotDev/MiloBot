package commands.morbconomy;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.Button;
import newdb.dao.UserDao;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EmbedUtils;
import utility.User;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Shows the user their own profile or that of someone else.
 */
public class ProfileCmd extends Command implements MorbconomyCmd {
	private final User user;
	private static final Logger logger = LoggerFactory.getLogger(ProfileCmd.class);
	private final UserDao userDao = UserDao.getInstance();

	public ProfileCmd() {
		this.commandName = "profile";
		this.commandDescription = "View your own or someone else's profile.";
		this.commandArgs = new String[]{"*user"};
		this.cooldown = 0;
		this.user = User.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		net.dv8tion.jda.api.entities.User author = event.getAuthor();
		if (args.size() == 0) {
			String name = author.getName();
			Optional<EmbedBuilder> embedBuilder = makeEmbed(name, author, author.getId());
			if (embedBuilder.isPresent()) {
				event.getChannel().sendMessageEmbeds(embedBuilder.get().build()).setActionRow(
						Button.secondary(author.getId() + ":delete", "Delete")).queue();
			} else {
				event.getChannel().sendMessage("Something went wrong.").queue();
			}
		} else {
			String findUser = String.join(" ", args);
			try {
				List<Member> usersByName = new ArrayList<>();
				event.getGuild().findMembers(e -> e.getUser().getName().toLowerCase(Locale.ROOT).equals(findUser)
								|| e.getUser().getAsMention().equals(findUser) || (e.getNickname() != null && e.getNickname().equals(findUser)))
						.onSuccess(members -> {
							usersByName.addAll(members);
							if (usersByName.size() == 0) {
								event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
							} else {
								net.dv8tion.jda.api.entities.User user = usersByName.get(0).getUser();
								String name = user.getName();
								Optional<EmbedBuilder> embed = makeEmbed(name, author, user.getId());
								if (embed.isPresent()) {
									event.getChannel().sendMessageEmbeds(embed.get().build()).setActionRow(
											Button.secondary(author.getId() + ":delete", "Delete")).queue();
								} else {
									event.getChannel().sendMessage(String.format("User `%s` not found.", findUser)).queue();
								}
							}
						});
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandEvent event) {
		event.deferReply().queue();
		net.dv8tion.jda.api.entities.User author = event.getUser();
		if(event.getOption("user") == null) {
			String name = author.getName();
			Optional<EmbedBuilder> embedBuilder = makeEmbed(name, author, author.getId());
			if (embedBuilder.isPresent()) {
				event.getHook().sendMessageEmbeds(embedBuilder.get().build()).addActionRow(
						Button.secondary(author.getId() + ":delete", "Delete")).queue();
			} else {
				event.getHook().sendMessage("Something went wrong.").queue();
			}
		} else {
			String findUser = Objects.requireNonNull(event.getOption("user")).getAsString();
			try {
				List<Member> usersByName = new ArrayList<>();
				Objects.requireNonNull(event.getGuild()).findMembers(e -> e.getUser().getId().equals(findUser))
						.onSuccess(members -> {
							usersByName.addAll(members);
							if (usersByName.size() == 0) {
								event.getHook().sendMessage(String.format("User `%s` not found.", findUser)).queue();
							} else {
								net.dv8tion.jda.api.entities.User user = usersByName.get(0).getUser();
								String name = user.getName();
								Optional<EmbedBuilder> embed = makeEmbed(name, author, user.getId());
								if (embed.isPresent()) {
									event.getHook().sendMessageEmbeds(embed.get().build()).addActionRow(
											Button.secondary(author.getId() + ":delete", "Delete")).queue();
								} else {
									event.getHook().sendMessage(String.format("Can't create a profile for `%s`.", name)).queue();
								}
							}
						});
			} catch (IllegalStateException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Builds the embed for the Profile command.
	 */
	private Optional<EmbedBuilder> makeEmbed(String name, net.dv8tion.jda.api.entities.User author, String id) {
		newdb.model.User userDbObj;
		try {
			userDbObj = userDao.getUserByDiscordId(author.getIdLong());
		} catch (SQLException e) {
			logger.error("Error getting user at making user embed at profile command", e);
			return Optional.empty();
		}
		int rank;
		try {
			rank = userDao.getUserRank(Objects.requireNonNull(userDbObj).getId());
		} catch (SQLException e) {
			logger.error("Error getting user rank at making user embed at profile command", e);
			return Optional.empty();
		}
		String userName = author.getName();
		int currency = userDbObj.getCurrency();
		int level = userDbObj.getLevel();
		int experience = userDbObj.getExperience();
		int userAmount = userDbObj.getCurrency();

		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, author);
		embed.setTitle(name);

		StringBuilder levelDescription = new StringBuilder();
		Optional<String> levelProgressBar = generateLevelProgressBar(level, experience);
		levelDescription.append(String.format("**Level:** `%s`\n", level));
		levelDescription.append(String.format("**Experience:** `%s`\n", experience));
		if (levelProgressBar.isPresent()) {
			levelDescription.append("**Progress till next level:** ");
			levelDescription.append(String.format("`%s`\n", levelProgressBar.get()));
		} else {
			levelDescription.append("You are at the maximum level.\n");
		}
		levelDescription.append(String.format("**Rank:** `%s`", rank));
		embed.setDescription(levelDescription.toString());

		return Optional.of(embed);
	}

	/**
	 * Generates a progress bar based on the amount of experience is needed for the next level.
	 */
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
