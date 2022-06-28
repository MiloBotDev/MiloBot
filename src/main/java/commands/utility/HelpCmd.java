package commands.utility;

import commands.Command;
import commands.CommandHandler;
import commands.CommandLoader;
import commands.bot.BotCmd;
import commands.economy.EconomyCmd;
import commands.games.GamesCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * The Help command.
 * Shows the user an overview of every command, or detailed information on a specific command.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class HelpCmd extends Command implements UtilityCmd {

	public HelpCmd() {
		this.commandName = "help";
		this.commandDescription = "Shows the user a list of available commands.";
		this.commandArgs = new String[]{"*command"};
	}

	@Override
	public void execute(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if (args.size() > 0) {
			CommandLoader.commandList.forEach((key, value) -> {
				if (key.contains(args.get(0).toLowerCase(Locale.ROOT))) {
					value.generateHelp(event, value.commandName, value.commandDescription, value.commandArgs,
							value.aliases, value.flags, value.cooldown, value.subCommands);
				}
			});
		} else {
			String prefix = CommandHandler.prefixes.get(event.getGuild().getId());

			String consumerName = event.getAuthor().getName();

			EmbedBuilder categoryEmbed = new EmbedBuilder();
			EmbedUtils.styleEmbed(event, categoryEmbed);
			categoryEmbed.setTitle("Categories 📝");
			categoryEmbed.setDescription("Click each categories respective emoji to see their commands.");
			categoryEmbed.addField("Utility 🔨", UtilityCmd.description, true);
			categoryEmbed.addField("Economy 💰", EconomyCmd.description, true);
			categoryEmbed.addField("Games 🎮", GamesCmd.description, true);
			categoryEmbed.addField("Bot 🤖", BotCmd.description, true);

			EmbedBuilder utilityEmbed = new EmbedBuilder();
			EmbedUtils.styleEmbed(event, utilityEmbed);
			utilityEmbed.setTitle("Utility Commands 🔨");

			EmbedBuilder economyEmbed = new EmbedBuilder();
			EmbedUtils.styleEmbed(event, economyEmbed);
			economyEmbed.setTitle("Economy Commands 💰");

			EmbedBuilder gamesEmbed = new EmbedBuilder();
			EmbedUtils.styleEmbed(event, gamesEmbed);
			gamesEmbed.setTitle("Game Commands 🎮");

			EmbedBuilder botEmbed = new EmbedBuilder();
			EmbedUtils.styleEmbed(event, botEmbed);
			botEmbed.setTitle("Bot Commands 🤖");

			CommandLoader.commandList.forEach((key, value) -> {
				if (value instanceof UtilityCmd) {
					utilityEmbed.addField(String.format("%s%s", prefix, value.commandName), value.commandDescription, true);
				} else if (value instanceof EconomyCmd) {
					economyEmbed.addField(String.format("%s%s", prefix, value.commandName), value.commandDescription, true);
				} else if (value instanceof GamesCmd) {
					gamesEmbed.addField(String.format("%s%s", prefix, value.commandName), value.commandDescription, true);
				} else if (value instanceof BotCmd) {
					botEmbed.addField(String.format("%s%s", prefix, value.commandName), value.commandDescription, true);
				}
			});

			Map<String, EmbedBuilder> embedAsEmoji = new HashMap<>();
			embedAsEmoji.put("📝", categoryEmbed);
			embedAsEmoji.put("🔨", utilityEmbed);
			embedAsEmoji.put("💰", economyEmbed);
			embedAsEmoji.put("🎮", gamesEmbed);
			embedAsEmoji.put("🤖", botEmbed);

			event.getChannel().sendTyping().queue();
			event.getChannel().sendMessageEmbeds(categoryEmbed.build()).queue(
					message -> {
						message.addReaction("📝").queue();
						message.addReaction("🔨").queue();
						message.addReaction("💰").queue();
						message.addReaction("🎮").queue();
						message.addReaction("🤖").queue();
						message.addReaction("❌").queue();
						ListenerAdapter listener = new ListenerAdapter() {
							@Override
							public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event1) {
								String messageId = event1.getMessageId();
								if (Objects.requireNonNull(event1.getUser()).getName().equals(consumerName)
										&& message.getId().equals(messageId)) {
									String asReactionCode = event1.getReactionEmote().getAsReactionCode();
									if (asReactionCode.equals("❌")) {
										event.getChannel().deleteMessageById(messageId).queue();
										event.getJDA().removeEventListener(this);
									} else {
										message.removeReaction(asReactionCode, event1.getUser()).queue();
										message.editMessageEmbeds(embedAsEmoji.get(asReactionCode).build()).queue();
									}
								}
							}
						};
						message.getJDA().getRateLimitPool().schedule(() -> event.getJDA().removeEventListener(listener),
								1, TimeUnit.MINUTES);
						message.getJDA().addEventListener(listener);
					});
		}
	}

}
