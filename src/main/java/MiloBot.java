import commands.CommandLoader;
import events.OnReadyEvent;
import events.guild.OnGuildJoin;
import events.guild.OnGuildLeave;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import utility.Config;

import javax.security.auth.login.LoginException;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {

    public static void main(String[] args) throws LoginException, InterruptedException {
        Config config = Config.getInstance();

        JDA bot = null;
        bot = JDABuilder.createDefault(config.getBotToken(),
                        GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_EMOJIS, GatewayIntent.GUILD_VOICE_STATES,
                        GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_TYPING,
                        GatewayIntent.DIRECT_MESSAGE_TYPING, GatewayIntent.DIRECT_MESSAGE_REACTIONS, GatewayIntent.GUILD_MESSAGE_REACTIONS,
                        GatewayIntent.DIRECT_MESSAGES)
                .setActivity(Activity.watching("Morbius"))
                .addEventListeners(new OnGuildJoin(), new OnGuildLeave(),
                        new OnReadyEvent())
                .build().awaitReady();

        CommandLoader.loadAllCommands(bot);

        // uncomment this line to generate documentation for all commands
        // CommandHandler.generateDocumentation();
    }
}