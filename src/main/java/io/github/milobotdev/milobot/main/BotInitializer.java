package io.github.milobotdev.milobot.main;

import io.github.milobotdev.milobot.commands.CommandLoader;
import io.github.milobotdev.milobot.commands.command.extensions.Instance;
import io.github.milobotdev.milobot.commands.instance.GameInstanceManager;
import io.github.milobotdev.milobot.events.EventLoader;
import io.github.milobotdev.milobot.commands.ButtonHandler;
import io.github.milobotdev.milobot.commands.GuildPrefixManager;

public class BotInitializer {

    /**
     * Starts the discord bot (JDA).
     */
    public static void initialize() {
        // put everything that has to be initialized in the bot here
        // keep this method as short as possible, you should only have one-liners here, like SomeClass.initialize()
        // or SomeClass.getInstance().initialize()
        // if you need more than one line to initialize something, it means that it should be in its own class
        GuildPrefixManager.getInstance().initialize();
        GameInstanceManager.getInstance().initialize();
        CommandLoader.initialize();
        ButtonHandler.getInstance().initialize();
        EventLoader.load();

        // load the bot
        JDAManager.getInstance().build();
    }
}
