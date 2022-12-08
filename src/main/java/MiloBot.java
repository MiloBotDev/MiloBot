import tk.milobot.BotInitializer;

import javax.security.auth.login.LoginException;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {

    public static void main(String[] args) {
        BotInitializer.initialize();

        // uncomment this line to generate documentation for all commands
        // CommandHandler.generateDocumentation();
    }
}