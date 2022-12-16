package io.github.milobotdev.milobot.commands.loaders;

import io.github.milobotdev.milobot.commands.ButtonHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommonLoader {

    public static void load() {
        ExecutorService genericButtonHandler = Executors.newSingleThreadExecutor();
        ButtonHandler buttonHandler = ButtonHandler.getInstance();

        buttonHandler.registerButton("delete", false, ButtonHandler.DeferType.NONE, genericButtonHandler,
                (event) -> event.getMessage().delete().queue());
    }
}
