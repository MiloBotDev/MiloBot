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
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import utility.EmbedUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Shows the user an overview of every command, or detailed information on a specific command.
 * This class is a singleton.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class HelpCmd extends Command implements UtilityCmd {

	private static HelpCmd instance;

	private ArrayList<List<ActionRow>> buttons;
	private EmbedBuilder categoryEmbed;
	private EmbedBuilder utilityEmbed;
	private EmbedBuilder botEmbed;
	private EmbedBuilder gamesEmbed;
	private EmbedBuilder economyEmbed;

	private HelpCmd() {
		this.commandName = "help";
		this.commandDescription = "Shows the user a list of available commands.";
		this.commandArgs = new String[]{"*command"};

	}

	/**
	 * Return the only instance of this class or make a new one if no instance exists.
	 */
	public static HelpCmd getInstance() {
		if(instance == null) {
			instance = new HelpCmd();
		}
		return instance;
	}

	@Override
	public void executeCommand(@NotNull MessageReceivedEvent event, @NotNull List<String> args) {
		String authorId = event.getAuthor().getId();
		if (args.size() > 0) {
			AtomicBoolean commandFound = new AtomicBoolean(false);
			String arg = args.get(0);
			CommandLoader.commandList.forEach((key, value) -> {
				if (key.contains(arg.toLowerCase(Locale.ROOT))) {
					EmbedBuilder embedBuilder = value.generateHelp(value.commandName, value.commandDescription,
							value.commandArgs, value.aliases, value.flags, value.cooldown, value.subCommands,
							event.getGuild(), event.getAuthor());
					event.getChannel().sendMessageEmbeds(embedBuilder.build()).setActionRow(
							Button.secondary(authorId + ":delete", "Delete")).queue();
					commandFound.set(true);
				}
			});
			if(!commandFound.get()) {
				event.getChannel().sendMessage(String.format("%s not found.", arg)).queue();
			}
		} else {
			createButtons(authorId);
			createEmbeds(event.getAuthor(), event.getGuild());
			EmbedUtils.styleEmbed(categoryEmbed, event.getAuthor());
			event.getChannel().sendMessageEmbeds(categoryEmbed.build()).setActionRows(buttons.get(0)).queue();
		}
	}

	@Override
	public void executeSlashCommand(@NotNull SlashCommandInteractionEvent event) {
		String authorId = event.getUser().getId();
		if(!(event.getOption("command") == null)) {
			AtomicBoolean commandFound = new AtomicBoolean(false);
			String command = Objects.requireNonNull(event.getOption("command")).getAsString();
			CommandLoader.commandList.forEach((key, value) -> {
				if (key.contains(command.toLowerCase(Locale.ROOT))) {
					EmbedBuilder embedBuilder = value.generateHelp(value.commandName, value.commandDescription,
							value.commandArgs, value.aliases, value.flags, value.cooldown, value.subCommands,
							Objects.requireNonNull(event.getGuild()), event.getUser());
					event.replyEmbeds(embedBuilder.build()).addActionRow(Button.secondary(authorId + ":delete", "Delete")).queue();
					commandFound.set(true);
				}
			});
			if(!commandFound.get()) {
				event.reply(String.format("%s not found.", command)).queue();
			}
		} else {
			createButtons(authorId);
			createEmbeds(event.getUser(), Objects.requireNonNull(event.getGuild()));
			EmbedUtils.styleEmbed(categoryEmbed, event.getUser());
			event.replyEmbeds(categoryEmbed.build()).addActionRows(buttons.get(0)).queue();
		}
	}

	/**
	 * Creates the buttons to attach to the help embed.
	 */
	private void createButtons(String authorId) {
		this.buttons = new ArrayList<>();

		buttons.add(List.of(ActionRow.of(
				Button.primary(authorId + ":categories", "Categories 📝"),
				Button.primary(authorId + ":utility", "Utility 🔨"),
				Button.primary(authorId + ":economy", "Economy 💰"),
				Button.primary(authorId + ":games", "Games 🎮"),
				Button.secondary(authorId + ":next", "Next"))
		));

		buttons.add(List.of(ActionRow.of(
				Button.secondary(authorId + ":previous", "Previous"),
				Button.primary(authorId + ":bot", "Bot 🤖"),
				Button.secondary(authorId + ":delete", "Delete"))
		));
	}

	/**
	 * Builds the embeds for the help command.
	 */
	private void createEmbeds(@NotNull User author, @NotNull Guild guild) {
		String prefix = CommandHandler.prefixes.get(guild.getId());

		this.categoryEmbed = new EmbedBuilder();
		categoryEmbed.setTitle("Categories 📝");
		categoryEmbed.setDescription("Use the buttons to navigate through the commands.");
		categoryEmbed.addField("Utility 🔨", UtilityCmd.description, true);
		categoryEmbed.addField("Economy 💰", EconomyCmd.description, true);
		categoryEmbed.addField("Games 🎮", GamesCmd.description, true);
		categoryEmbed.addField("Bot 🤖", BotCmd.description, true);

		this.utilityEmbed = new EmbedBuilder();
		utilityEmbed.setTitle("Utility Commands 🔨");

		this.economyEmbed = new EmbedBuilder();
		economyEmbed.setTitle("Economy Commands 💰");

		this.gamesEmbed = new EmbedBuilder();
		gamesEmbed.setTitle("Game Commands 🎮");

		this.botEmbed = new EmbedBuilder();
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
	}

	public ArrayList<List<ActionRow>> getButtons() {
		return buttons;
	}

	public EmbedBuilder getCategoryEmbed() {
		return categoryEmbed;
	}

	public EmbedBuilder getUtilityEmbed() {
		return utilityEmbed;
	}

	public EmbedBuilder getBotEmbed() {
		return botEmbed;
	}

	public EmbedBuilder getGamesEmbed() {
		return gamesEmbed;
	}

	public EmbedBuilder getEconomyEmbed() {
		return economyEmbed;
	}

}
