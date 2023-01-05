package io.github.milobotdev.milobot.main;

import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {

    public static void main(String[] args) {
        // Glassfish Jersey and Grizzly (for API) use java.util.logging, so we need to redirect java.util.logging to slf4j
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        BotInitializer.initialize();
        ApiInitializer.initialize();

        // uncomment this line to generate documentation for all commands
        // CommandHandler.generateDocumentation();
    }
}