package commands.dnd;

import commands.Command;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.EmbedUtils;
import utility.EncounterGenerator;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Lets users generate a random d&d encounter.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class EncounterCmd extends Command implements DndCmd {

	private final static Logger logger = LoggerFactory.getLogger(EncounterCmd.class);

	private final String[] difficulties;
	private final String[] environments;
	private final EncounterGenerator gen;

	public EncounterCmd() {
		this.commandName = "encounter";
		this.commandDescription = "Generate a random encounter for a given average party level, party size, " +
				"difficulty and an optional environment.";
		this.commandArgs = new String[]{"party size, party level, difficulty, *environment"};

		this.difficulties = new String[]{"easy", "medium", "difficult", "deadly"};
		this.environments = new String[]{"city", "dungeon", "forest", "nature", "other plane", "underground", "water"};
		this.gen = EncounterGenerator.getInstance();
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if(args.size() < 3) {
			sendCommandUsage(event, this.commandName, this.commandArgs);
			return;
		}
		int partySize;
		int partyLevel;
		try {
			partySize = Integer.parseInt(args.get(0));
			partyLevel = Integer.parseInt(args.get(1));
		} catch (NumberFormatException e) {
			event.getChannel().sendMessage("Party size and level must both be numbers.").queue();
			logger.error(e.getMessage());
			return;
		}
		if(partySize < 1 || partySize > 10) {
			event.getChannel().sendMessage("Party size must be a number between 1 and 10").queue();
			return;
		}
		if(partyLevel < 1 || partyLevel > 20) {
			event.getChannel().sendMessage("Party level must be a number between 1 and 20").queue();
			return;
		}
		String difficulty = args.get(2);
		if(!(Arrays.asList(difficulties).contains(difficulty.toLowerCase(Locale.ROOT)))) {
			event.getChannel().sendMessage(String.format("%s is not a valid difficulty. Please choose one of: " +
					"%s, %s, %s, %s.", difficulty, difficulties[0], difficulties[1], difficulties[2], difficulties[3])).queue();
			return;
		}
		int difficultyAsInt = Arrays.asList(difficulties).indexOf(difficulty.toLowerCase(Locale.ROOT)) + 1;
		String environment = null;
		if(args.size() > 3) {
			environment = args.get(3);
			if(args.size() > 4) {
				environment += String.format(" %s", args.get(4));
			}
			if(!(Arrays.asList(environments).contains(environment.toLowerCase(Locale.ROOT)))) {
				StringBuilder envError = new StringBuilder();
				envError.append(String.format("%s is not a valid environment. Please choose one of: ", environment));
				for(int i = 0; i <  environments.length; i++) {
					envError.append(environments[i]);
					if(i + 1 == environments.length) {
						envError.append(".");
					}
				}
				event.getChannel().sendMessage(envError.toString()).queue();
				return;
			}
		}
		EmbedBuilder embed = buildEncounterEmbed(event.getAuthor(), partySize, partyLevel, difficulty, difficultyAsInt, environment);
		event.getChannel().sendMessageEmbeds(embed.build()).setActionRow(Button.secondary(event.getAuthor().getId()
				+ ":delete", "Delete")).queue();
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		int partySize = Objects.requireNonNull(event.getOption("size")).getAsInt();
		int partyLevel = Objects.requireNonNull(event.getOption("level")).getAsInt();
		String difficulty = Objects.requireNonNull(event.getOption("difficulty")).getAsString();
		int difficultyAsInt = Arrays.asList(difficulties).indexOf(difficulty.toLowerCase(Locale.ROOT)) + 1;
		String environment = null;
		if(!(event.getOption("environment") == null)) {
			environment = Objects.requireNonNull(event.getOption("environment")).getAsString();
		}
		EmbedBuilder embedBuilder = buildEncounterEmbed(event.getUser(), partySize, partyLevel, difficulty, difficultyAsInt, environment);
		event.replyEmbeds(embedBuilder.build()).addActionRow(Button.secondary(event.getUser().getId() + ":delete", "Delete")).queue();
	}

	@NotNull
	private EmbedBuilder buildEncounterEmbed(@NotNull User author, int partySize, int partyLevel, String difficulty,
											 int difficultyAsInt, String environment) {
		String encounter = gen.generateEncounter(partySize, partyLevel, difficultyAsInt, environment);
		EmbedBuilder embed = new EmbedBuilder();
		EmbedUtils.styleEmbed(embed, author);
		embed.setTitle("Generated encounter");
		String desc = String.format("**Party Size:** %d\n**Party Level:** %d\n**Difficulty:** %s\n",
				partySize, partyLevel, difficulty);
		if(environment != null) {
			desc += String.format("**Environment:** %s", environment);
		}
		embed.setDescription(desc);
		embed.addField("Encounter", encounter, false);
		return embed;
	}

}