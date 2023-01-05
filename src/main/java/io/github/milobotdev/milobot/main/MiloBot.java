package io.github.milobotdev.milobot.main;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {

    public static void main(String[] args) {
        BotInitializer.initialize();
        ApiInitializer.initialize();

        // uncomment this line to generate documentation for all commands
        // CommandHandler.generateDocumentation();
    }
}