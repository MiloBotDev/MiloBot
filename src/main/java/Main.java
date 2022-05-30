import commands.CommandHandler;
import events.OnGuildJoinEvent;
import events.OnGuildLeaveEvent;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;

import javax.security.auth.login.LoginException;

/**
 * The Main class from where the bot is ran.
 * @author Ruben Eekhof - rubeneekhof@gmail.com
 */
public class Main {

    private static final String BOT_TOKEN = "OTIwMzE1OTg1NjI0NDMyNzIw.Ggjv_U.ZRNUFC5lMZJyjRrmByb1pnlNJuW2iIjRfDWcBI";
    public static JDA bot;

    public static void main(String[] args) throws LoginException, InterruptedException {
        bot = JDABuilder.createDefault(BOT_TOKEN)
                .setActivity(Activity.playing("IdleAway!"))
                .addEventListeners(new CommandHandler(), new OnGuildJoinEvent(), new OnGuildLeaveEvent())
                .build()
                .awaitReady();
    }
}
