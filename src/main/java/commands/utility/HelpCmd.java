package commands.utility;

import commands.Command;
import commands.CommandHandler;
import commands.CommandLoader;
import commands.bot.BotCmd;
import commands.economy.EconomyCmd;
import commands.games.GamesCmd;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
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
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		if (args.size() > 0) {
			String arg = args.get(0);
			CommandLoader.commandList.forEach((key, value) -> {
				if (key.contains(arg.toLowerCase(Locale.ROOT))) {
					EmbedBuilder embedBuilder = value.generateHelp(value.commandName, value.commandDescription,
							value.commandArgs, value.aliases, value.flags, value.cooldown, value.subCommands,
							event.getGuild(), event.getAuthor());
					event.getChannel().sendMessageEmbeds(embedBuilder.build()).queue();
				}
			});
			event.getChannel().sendMessage(String.format("%s not found.", arg)).queue();
		} else {
			buildHelpEmbed(event.getAuthor(), event.getGuild(), event);
		}
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		if(event.getOption("command") == null) {
			buildHelpEmbed(event.getUser(), Objects.requireNonNull(event.getGuild()), event);
		} else {
			AtomicBoolean commandFound = new AtomicBoolean(false);
			String command = Objects.requireNonNull(event.getOption("command")).getAsString();
			CommandLoader.commandList.forEach((key, value) -> {
				if (key.contains(command.toLowerCase(Locale.ROOT))) {
					EmbedBuilder embedBuilder = value.generateHelp(value.commandName, value.commandDescription,
							value.commandArgs, value.aliases, value.flags, value.cooldown, value.subCommands,
							Objects.requireNonNull(event.getGuild()), event.getUser());
					event.replyEmbeds(embedBuilder.build()).queue();
					commandFound.set(true);
				}
			});
			if(!commandFound.get()) {
				event.reply(String.format("%s not found.", command)).queue();
			}
		}
	}

	/**
	 * Builds and sends the embed for the help command.
	 */
	private void buildHelpEmbed(@NotNull User author, @NotNull Guild guild, Event event) {
		String prefix = CommandHandler.prefixes.get(guild.getId());

		String consumerName = author.getName();

		EmbedBuilder categoryEmbed = new EmbedBuilder();
		EmbedUtils.styleEmbed(categoryEmbed, author);
		categoryEmbed.setTitle("Categories 📝");
		categoryEmbed.setDescription("Click each categories respective emoji to see their commands.");
		categoryEmbed.addField("Utility 🔨", UtilityCmd.description, true);
		categoryEmbed.addField("Economy 💰", EconomyCmd.description, true);
		categoryEmbed.addField("Games 🎮", GamesCmd.description, true);
		categoryEmbed.addField("Bot 🤖", BotCmd.description, true);

		EmbedBuilder utilityEmbed = new EmbedBuilder();
		EmbedUtils.styleEmbed(utilityEmbed, author);
		utilityEmbed.setTitle("Utility Commands 🔨");

		EmbedBuilder economyEmbed = new EmbedBuilder();
		EmbedUtils.styleEmbed(economyEmbed, author);
		economyEmbed.setTitle("Economy Commands 💰");

		EmbedBuilder gamesEmbed = new EmbedBuilder();
		EmbedUtils.styleEmbed(gamesEmbed, author);
		gamesEmbed.setTitle("Game Commands 🎮");

		EmbedBuilder botEmbed = new EmbedBuilder();
		EmbedUtils.styleEmbed(botEmbed, author);
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

		if(event instanceof MessageReceivedEvent) {
			MessageReceivedEvent messageReceivedEvent = (MessageReceivedEvent) event;
			messageReceivedEvent.getChannel().sendMessageEmbeds(categoryEmbed.build()).queue(
					message -> {
						message.addReaction("📝").queue();
						message.addReaction("🔨").queue();
						message.addReaction("💰").queue();
						message.addReaction("🎮").queue();
						message.addReaction("🤖").queue();
						message.addReaction("⏹").queue();
						ListenerAdapter listener = new ListenerAdapter() {
							@Override
							public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event1) {
								String messageId = event1.getMessageId();
								if (Objects.requireNonNull(event1.getUser()).getName().equals(consumerName)
										&& message.getId().equals(messageId)) {
									String asReactionCode = event1.getReactionEmote().getAsReactionCode();
									if (asReactionCode.equals("⏹")) {
										messageReceivedEvent.getChannel().deleteMessageById(messageId).queue();
										messageReceivedEvent.getJDA().removeEventListener(this);
									} else {
										message.removeReaction(asReactionCode, event1.getUser()).queue();
										message.editMessageEmbeds(embedAsEmoji.get(asReactionCode).build()).queue();
									}
								}
							}
						};
						message.getJDA().getRateLimitPool().schedule(() -> messageReceivedEvent.getJDA().removeEventListener(listener),
								1, TimeUnit.MINUTES);
						message.getJDA().addEventListener(listener);
					});
		} else if(event instanceof SlashCommandInteractionEvent) {
			SlashCommandInteractionEvent slashEvent = (SlashCommandInteractionEvent) event;
			slashEvent.getChannel().sendMessageEmbeds(categoryEmbed.build()).queue(
					message -> {
						message.addReaction("📝").queue();
						message.addReaction("🔨").queue();
						message.addReaction("💰").queue();
						message.addReaction("🎮").queue();
						message.addReaction("🤖").queue();
						message.addReaction("⏹").queue();
						ListenerAdapter listener = new ListenerAdapter() {
							@Override
							public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event1) {
								String messageId = event1.getMessageId();
								if (Objects.requireNonNull(event1.getUser()).getName().equals(consumerName)
										&& message.getId().equals(messageId)) {
									String asReactionCode = event1.getReactionEmote().getAsReactionCode();
									if (asReactionCode.equals("⏹")) {
										slashEvent.getChannel().deleteMessageById(messageId).queue();
										slashEvent.getJDA().removeEventListener(this);
									} else {
										message.removeReaction(asReactionCode, event1.getUser()).queue();
										message.editMessageEmbeds(embedAsEmoji.get(asReactionCode).build()).queue();
									}
								}
							}
						};
						message.getJDA().getRateLimitPool().schedule(() -> slashEvent.getJDA().removeEventListener(listener),
								1, TimeUnit.MINUTES);
						message.getJDA().addEventListener(listener);
					});
			slashEvent.reply("Sending help").queue();
		}
	}

}
