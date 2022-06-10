import commands.CommandHandler;
import commands.CommandLoader;
import database.DatabaseManager;
import events.OnGuildJoinEvent;
import events.OnGuildLeaveEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import utility.Config;

import javax.security.auth.login.LoginException;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * The Main class from where the bot is ran.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Main {

    public static JDA bot;

    public static void main(String[] args) throws LoginException, InterruptedException, FileNotFoundException {
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        Connection connect = databaseManager.connect();
        // checks if the database exists and creates a new one if needed
        if(connect == null) {
            databaseManager.createNewDatabase();
        }
        databaseManager.createAndFillAllTables();

        CommandLoader.loadAllCommands();

        bot = JDABuilder.createDefault(Config.getInstance().botToken)
                .setActivity(Activity.playing("IdleAway!"))
                .addEventListeners(new CommandHandler(), new OnGuildJoinEvent(), new OnGuildLeaveEvent())
                .build()
                .awaitReady();
    }
}
