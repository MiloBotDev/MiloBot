package tk.milobot;

import commands.GuildPrefixManager;
import commands.NewCommandLoader;

public class BotInitializer {

    public static void initialize() {
        // put everything that has to be initialized in the bot here
        // keep this method as short as possible, you should only have one-liners here, like SomeClass.initialize()
        // or SomeClass.getInstance().initialize()
        // if you need more than one line to initialize something, it means that it should be in its own class
        GuildPrefixManager.getInstance().initialize();
        NewCommandLoader.initialize();

        // load the bot
        JDAManager.getInstance().build();
    }
}
