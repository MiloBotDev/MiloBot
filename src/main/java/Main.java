import commands.CommandHandler;
import commands.CommandLoader;
import database.DatabaseManager;
import events.OnGuildJoinEvent;
import events.OnGuildLeaveEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import utility.Config;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.sql.Connection;

/**
 * The Main class from where the bot is ran.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Main {

    public static void main(String[] args) throws LoginException, FileNotFoundException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        Connection connect = databaseManager.connect();
        // checks if the database exists and creates a new one if needed
        if(connect == null) {
            databaseManager.createNewDatabase();
        }
        databaseManager.createAndFillAllTables();

        CommandLoader.loadAllCommands();

        JDA bot = JDABuilder.createDefault(Config.getInstance().botToken,
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .setActivity(Activity.playing("IdleAway!"))
                .addEventListeners(new CommandHandler(), new OnGuildJoinEvent(), new OnGuildLeaveEvent())
                .build();
    }
}
