package io.github.milobotdev.milobot.main;

import io.github.milobotdev.milobot.utility.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

/**
 * The Main class from where the bot is started.
 */
public class MiloBot {
    private static final Logger logger = LoggerFactory.getLogger(MiloBot.class);

    public static void main(String[] args) {

        boolean somethingInitialized = false;
        if (Config.getInstance().isBotEnabled()) {
            logger.debug("Bot enabled - initializing bot");
            BotInitializer.initialize();
            somethingInitialized = true;
        } else {
            logger.debug("Bot disabled - skipping bot initialization");
        }
        if (Config.getInstance().isApiEnabled()) {
            logger.debug("API enabled - initializing API");
            // Glassfish Jersey and Grizzly (for API) use java.util.logging, so we need to redirect java.util.logging to slf4j
            SLF4JBridgeHandler.removeHandlersForRootLogger();
            SLF4JBridgeHandler.install();
            ApiInitializer.initialize();
            somethingInitialized = true;
        } else {
            logger.debug("API disabled - skipping API initialization");
        }

        if (!somethingInitialized) {
            logger.warn("Nothing set to enabled in config, bot has nothing to do - exiting");
        }

        // uncomment this line to generate documentation for all commands
        // CommandHandler.generateDocumentation();
    }
}