package events;

import commands.dnd.encounter.EncounterGeneratorCmd;
import commands.games.wordle.WordleLeaderboardCmd;
import commands.utility.HelpCmd;
import database.queries.WordleTableQueries;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;
import utility.Paginator;

import java.util.ArrayList;
import java.util.List;

/**
 * Triggers when a button is clicked by a user.
 */
public class OnButtonInteractionEvent extends ListenerAdapter {

	private final HelpCmd helpCmd;
	private final EncounterGeneratorCmd encCmd;

	public OnButtonInteractionEvent() {
		this.helpCmd = HelpCmd.getInstance();
		this.encCmd = EncounterGeneratorCmd.getInstance();
	}

	@Override
	public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
		String[] id = event.getComponentId().split(":");
		String authorId = id[0];
		String type = id[1];
		// Check that the button is for the user that clicked it, otherwise just ignore the event (let interaction fail)
		User user = event.getUser();
		if (!authorId.equals(user.getId()))
			return;
		event.deferEdit().queue(); // acknowledge the button was clicked, otherwise the interaction will fail

		MessageChannel channel = event.getChannel();
		switch (type) {
			case "delete":
				event.getHook().deleteOriginal().queue();
				break;
			case "nextPage":
				Paginator paginator = Paginator.paginatorInstances.get(event.getMessage().getId());
				if(paginator != null) {
					paginator.nextPage().ifPresent(embed -> event.getHook().editOriginalEmbeds(embed.build()).queue());
				}
				break;
			case "previousPage":
				Paginator paginator2 = Paginator.paginatorInstances.get(event.getMessage().getId());
				if(paginator2 != null) {
					paginator2.previousPage().ifPresent(embed -> event.getHook().editOriginalEmbeds(embed.build()).queue());
				}
				break;
			case "regenerate":
				MessageEmbed build = encCmd.regenerateEncounter(event.getMessage().getEmbeds().get(0), event.getUser());
				event.getHook().editOriginalEmbeds(build).setActionRows(
						ActionRow.of(Button.primary(event.getUser().getId() + ":regenerate", "Regenerate"),
								Button.primary(event.getUser().getId() + ":save", "Save"),
								Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
				break;
			case "save":
				event.getHook().editOriginalEmbeds(event.getMessage().getEmbeds()).setActionRows(ActionRow.of(
						Button.primary(event.getUser().getId() + ":regenerate", "Regenerate"),
						Button.secondary(event.getUser().getId() + ":delete", "Delete"))).queue();
				encCmd.saveEncounter(event.getMessage().getEmbeds().get(0), event.getUser());
				break;
			case "totalGamesPlayed":
				ArrayList<EmbedBuilder> totalGamesPlayedEmbeds = WordleLeaderboardCmd.makeLeaderboardEmbeds(event.getUser(), "Top 100: total games played",
						WordleTableQueries.wordleGetTopTotalGamesPlayed);
				Paginator totalGamesPlayedPager = new Paginator(totalGamesPlayedEmbeds.get(0));
				totalGamesPlayedEmbeds.remove(0);
				totalGamesPlayedPager.addPages(totalGamesPlayedEmbeds);
				event.getHook().editOriginalEmbeds(totalGamesPlayedPager.currentPage().build()).setActionRows(ActionRow.of(
						Button.primary(event.getUser().getId() + ":previousPage", "Previous"),
						Button.secondary(event.getUser().getId() + ":delete", "Delete"),
						Button.primary(event.getUser().getId() + ":nextPage", "Next")
						)).queue(message -> totalGamesPlayedPager.initialize(event.getMessageId()));
				break;
			case "highestStreak":
				ArrayList<EmbedBuilder> highestStreakEmbeds = WordleLeaderboardCmd.makeLeaderboardEmbeds(event.getUser(),"Top 100: highest streak",
						WordleTableQueries.wordleGetTopHighestStreak);
				Paginator highestStreakPager = new Paginator(highestStreakEmbeds.get(0));
				highestStreakEmbeds.remove(0);
				highestStreakPager.addPages(highestStreakEmbeds);
				event.getHook().editOriginalEmbeds(highestStreakPager.currentPage().build()).setActionRows(ActionRow.of(
						Button.primary(event.getUser().getId() + ":previousPage", "Previous"),
						Button.secondary(event.getUser().getId() + ":delete", "Delete"),
						Button.primary(event.getUser().getId() + ":nextPage", "Next")
				)).queue(message -> highestStreakPager.initialize(event.getMessageId()));
				break;
			case "currentStreak":
				ArrayList<EmbedBuilder> currentStreakEmbeds = WordleLeaderboardCmd.makeLeaderboardEmbeds(event.getUser(),"Top 100: current streak",
						WordleTableQueries.wordleGetTopCurrentStreak);
				Paginator currentStreakPager = new Paginator(currentStreakEmbeds.get(0));
				currentStreakEmbeds.remove(0);
				currentStreakPager.addPages(currentStreakEmbeds);
				event.getHook().editOriginalEmbeds(currentStreakPager.currentPage().build()).setActionRows(ActionRow.of(
						Button.primary(event.getUser().getId() + ":previousPage", "Previous"),
						Button.secondary(event.getUser().getId() + ":delete", "Delete"),
						Button.primary(event.getUser().getId() + ":nextPage", "Next")
				)).queue(message -> currentStreakPager.initialize(event.getMessageId()));
				break;
		}
	}
}
