import commands.CommandHandler;
import commands.CommandLoader;
import database.DatabaseManager;
import events.OnGuildJoinEvent;
import events.OnGuildLeaveEvent;
import events.OnReadyEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utility.Config;

import javax.security.auth.login.LoginException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * The Main class from where the bot is started.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Main {

	private final static Logger logger = LoggerFactory.getLogger(Main.class);

	public static void main(String[] args) throws LoginException, InterruptedException {
		DatabaseManager databaseManager = DatabaseManager.getInstance();
		Connection connect = databaseManager.connect();
		// checks if the database exists and creates a new one if needed
		if (connect == null) {
			logger.info("No existing database found.");
			databaseManager.createNewDatabase();
			databaseManager.createAndFillAllTables();
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
						new OnReadyEvent())
				.build().awaitReady();

		loadPrefixes(databaseManager, config, bot);
	}

	/**
	 * Loads prefixes for guilds that have the bot but are not in the database yet.
	 * @param databaseManager - The instance of the DatabaseManager
	 * @param config - The instance of the Config
	 * @param bot - The instance of the JDA
	 */
	private static void loadPrefixes(@NotNull DatabaseManager databaseManager, Config config, @NotNull JDA bot) {
		List<Guild> guilds = bot.getGuilds();
		ArrayList<String> result = databaseManager.query(databaseManager.getAllPrefixes, DatabaseManager.QueryTypes.RETURN);
		for(Guild guild : guilds) {
			String id = guild.getId();
			if(!result.contains(id)) {
				logger.info(String.format("Guild: %s does not have a configured prefix.", id));
				databaseManager.query(databaseManager.addServerPrefix, DatabaseManager.QueryTypes.UPDATE, id, config.defaultPrefix);
				CommandHandler.prefixes.put(id, config.defaultPrefix);
			}
		}
	}
}
