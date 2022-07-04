import commands.CommandHandler;
import commands.CommandLoader;
import database.DatabaseManager;
import database.queries.PrefixTableQueries;
import database.queries.UserTableQueries;
import events.OnReadyEvent;
import events.OnUserUpdateNameEvent;
import events.guild.OnGuildJoinEvent;
import events.guild.OnGuildLeaveEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Main class from where the bot is started.
 *
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class MiloBot {

	private final static Logger logger = LoggerFactory.getLogger(MiloBot.class);

	public static void main(String[] args) throws LoginException, InterruptedException {
		DatabaseManager manager = DatabaseManager.getInstance();
		Connection connect = manager.connect();
		// checks if the database exists and creates a new one if needed
		if (connect == null) {
			logger.info("No existing database found.");
			manager.createNewDatabase();
			manager.createAndFillAllTables();
		}
		// loads the config file
		Config config = Config.getInstance();

		CommandLoader.loadAllCommands();

		JDA bot = JDABuilder.createDefault(config.botToken,
						GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
						GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
						GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS)
				.setActivity(Activity.watching("Morbius"))
				.addEventListeners(new CommandHandler(), new OnGuildJoinEvent(), new OnGuildLeaveEvent(),
						new OnReadyEvent(), new OnUserUpdateNameEvent())
				.build().awaitReady();

		CommandListUpdateAction commands = bot.updateCommands();
		commands.addCommands(Commands.slash("help", "the help command")
				.addOption(OptionType.STRING, "command", "The command you want help on", false))
				.queue();

		loadPrefixes(manager, config, bot);
		updateUserNames(manager, bot);
	}

	/**
	 * Loads prefixes for guilds that have the bot but are not in the database yet.
	 */
	private static void loadPrefixes(@NotNull DatabaseManager manager, Config config, @NotNull JDA bot) {
		List<Guild> guilds = bot.getGuilds();
		ArrayList<String> result = manager.query(PrefixTableQueries.getAllPrefixes, DatabaseManager.QueryTypes.RETURN);
		for (Guild guild : guilds) {
			String id = guild.getId();
			if (!result.contains(id)) {
				logger.info(String.format("Guild: %s does not have a configured prefix.", id));
				manager.query(PrefixTableQueries.addServerPrefix, DatabaseManager.QueryTypes.UPDATE, id, config.defaultPrefix);
				CommandHandler.prefixes.put(id, config.defaultPrefix);
			}
		}
	}

	/**
	 * Checks if any users updated their name. If so update it in the database.
	 */
	private static void updateUserNames(@NotNull DatabaseManager manager, @NotNull JDA bot) {
		List<Guild> guilds = bot.getGuilds();
		ArrayList<String> result = manager.query(UserTableQueries.getAllUserIdsAndNames, DatabaseManager.QueryTypes.RETURN);
		HashMap<String, String> users = new HashMap<>();
		for (int i = 0; i < result.size(); i += 2) {
			if (!(i + 2 == result.size() && users.containsKey(result.get(i)))) {
				users.put(result.get(i), result.get(i + 1));
			}
		}
		for (Guild guild : guilds) {
			List<Member> members = new ArrayList<>();
			guild.loadMembers().onSuccess(loadedMembers -> {
				members.addAll(loadedMembers);
				for (Member member : members) {
					User user = member.getUser();
					if (!(user.isBot())) {
						String userId = user.getId();
						if (users.containsKey(userId)) {
							String nameInDatabase = users.get(userId);
							String currentName = user.getName();
							if (!(nameInDatabase.equals(currentName))) {
								logger.info(String.format("%s changed their name to: %s.", nameInDatabase, currentName));
								manager.query(UserTableQueries.updateUserName, DatabaseManager.QueryTypes.UPDATE, currentName, userId);
							}
						}
					}
				}
			});

		}
	}
}
