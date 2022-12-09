package tk.milobot.commands;

import tk.milobot.commands.loaders.BlackjackLoader;

public class NewCommandLoader {

    public static void initialize() {
        // put every command initializer call here
        // keep this method as short as possible, you should only have one-liners here, like SomeClass.load()
        // or SomeClass.getInstance().load()
        // if you need more than one line to initialize something, it means that it should be in its own class

        BlackjackLoader.load();
    }
}
