package events;

import commands.utility.HelpCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.List;

/**
 * Triggers when a button is clicked by a user.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class OnButtonInteractionEvent extends ListenerAdapter {

	private final HelpCmd helpCmd;

	public OnButtonInteractionEvent() {
		this.helpCmd = HelpCmd.getInstance();
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
			case "categories":
				EmbedBuilder categoryEmbed = helpCmd.getCategoryEmbed();
				EmbedUtils.styleEmbed(categoryEmbed, user);
				event.getHook().editOriginalEmbeds(categoryEmbed.build()).queue();
				break;
			case "utility":
				EmbedBuilder utilityEmbed = helpCmd.getUtilityEmbed();
				EmbedUtils.styleEmbed(utilityEmbed, user);
				event.getHook().editOriginalEmbeds(utilityEmbed.build()).queue();
				break;
			case "economy":
				EmbedBuilder economyEmbed = helpCmd.getEconomyEmbed();
				EmbedUtils.styleEmbed(economyEmbed, user);
				event.getHook().editOriginalEmbeds(economyEmbed.build()).queue();
				break;
			case "games":
				EmbedBuilder gamesEmbed = helpCmd.getGamesEmbed();
				EmbedUtils.styleEmbed(gamesEmbed, user);
				event.getHook().editOriginalEmbeds(gamesEmbed.build()).queue();
				break;
			case "bot":
				EmbedBuilder botEmbed = helpCmd.getBotEmbed();
				EmbedUtils.styleEmbed(botEmbed, user);
				event.getHook().editOriginalEmbeds(botEmbed.build()).queue();
				break;
			case "next":
				List<MessageEmbed> embeds = event.getMessage().getEmbeds();
				event.getHook().editOriginalEmbeds(embeds).setActionRows(helpCmd
						.getButtons().get(1)).queue();
				break;
			case "previous":
				event.getHook().editOriginalEmbeds(event.getMessage().getEmbeds()).setActionRows(helpCmd
						.getButtons().get(0)).queue();
				break;
		}
	}
}