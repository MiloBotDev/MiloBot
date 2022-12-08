package tk.milobot.main;

import tk.milobot.commands.ButtonHandler;
import tk.milobot.commands.GuildPrefixManager;
import tk.milobot.commands.NewCommandLoader;

public class BotInitializer {

    public static void initialize() {
        // put everything that has to be initialized in the bot here
        // keep this method as short as possible, you should only have one-liners here, like SomeClass.initialize()
        // or SomeClass.getInstance().initialize()
        // if you need more than one line to initialize something, it means that it should be in its own class
        GuildPrefixManager.getInstance().initialize();
        NewCommandLoader.initialize();
        ButtonHandler.getInstance().initialize();

        // load the bot
        JDAManager.getInstance().build();
    }
}
