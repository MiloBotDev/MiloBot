package io.github.milobotdev.milobot.main;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.milobotdev.milobot.utility.Config;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Manages the bot JDA.
 */
public class JDAManager {

    private static JDAManager instance;
    private final Logger logger = LoggerFactory.getLogger(JDAManager.class);
    private final JDABuilder jdaBuilder;
    private volatile boolean built = false;
    private List<Consumer<JDA>> jdaBuiltActions = new ArrayList<>();
    private List<Consumer<JDA>> jdaReadyActions = new ArrayList<>();
    private JDA jda;

    private JDAManager() {
        jdaBuilder = JDABuilder.createDefault(Config.getInstance().getBotToken())
                .setActivity(Activity.listening("Ping bot for help"))
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT);
    }

    public synchronized static JDAManager getInstance() {
        if (instance == null) {
            instance = new JDAManager();
        }
        return instance;
    }

    /**
     * Returns the JDA builder.
     * @return the JDA builder
     */
    public JDABuilder getJDABuilder() {
        if (!built) {
            return jdaBuilder;
        } else {
            throw new IllegalStateException("JDA has already been built");
        }
    }

    /**
     * Builds the JDA builder.
     */
    public synchronized void build() {
        if (!built) {
            built = true;
            // TODO: figure out exception thrown when JDA login fails and handle it appropriately
            //try {
                jda = jdaBuilder.build();
            //} //catch (LoginException e) {
               // logger.error("FATAL ERROR: JDA login failed. Bot cannot continue from this state.", e);
               // return;
           // }
            logger.debug("JDA login successful.");
            jdaBuiltActions.forEach(action -> action.accept(jda));
            logger.trace("JDA build actions complete.");
            if (!jdaReadyActions.isEmpty()) {
                boolean success = true;
                try {
                    jda.awaitReady();
                } catch (InterruptedException e) {
                    logger.error("JDA was interrupted while waiting for ready state. " +
                            "JDA ready actions weren't called.", e);
                    success = false;
                }
                if (success) {
                    jdaReadyActions.forEach(action -> action.accept(jda));
                    logger.trace("JDA ready actions complete.");
                }
            } else {
                logger.trace("No JDA ready actions to run.");
            }
            // set jda build and ready action lists to null to free up memory
            jdaBuiltActions = null;
            jdaReadyActions = null;
            logger.info("Bot loaded.");
        } else {
            throw new IllegalStateException("JDA has already been built");
        }
    }

    /**
     * Adds a JDA build action to the bot. This action will be called immediately after the JDA has been built.
     * @param action the action to be called
     */
    public void addJdaBuiltAction(Consumer<JDA> action) {
        if (!built) {
            jdaBuiltActions.add(action);
        } else {
            throw new IllegalStateException("JDA has already been built");
        }
    }

    /**
     * Adds a JDA ready action to the bot. This action will be called after the JDA has been built and is ready.
     * Only use this method if you need to wait for the JDA to be ready. Otherwise, use {@link #addJdaBuiltAction(Consumer)}.
     * Using this method when unnecessarily can increase the time it takes for the bot to load.
     * @param action the action to be called
     */
    public void addJdaReadyAction(Consumer<JDA> action) {
        if (!built) {
            jdaReadyActions.add(action);
        } else {
            throw new IllegalStateException("JDA has already been built");
        }
    }

    /**
     * Returns the JDA.
     * @return the JDA
     */
    public JDA getJDA() {
        if (built) {
            return jda;
        } else {
            throw new IllegalStateException("JDA has not been built");
        }
    }
}
