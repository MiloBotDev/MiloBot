package io.github.milobotdev.milobot.utility.paginator;

import io.github.milobotdev.milobot.commands.ButtonHandler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PaginatorLoader {

    public static void load() {
        ButtonHandler buttonHandler = ButtonHandler.getInstance();

        ExecutorService paginatorService = Executors.newSingleThreadExecutor();
        buttonHandler.registerButton("nextPage", true, ButtonHandler.DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.nextPage();
            }
        });

        buttonHandler.registerButton("previousPage", true, ButtonHandler.DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.previousPage();
            }
        });

        buttonHandler.registerButton("deletePaginator", true, ButtonHandler.DeferType.EDIT, paginatorService, (event) -> {
            Paginator paginator = Paginator.getPaginatorByMessage(event.getMessage());
            if (paginator != null) {
                paginator.remove();
            }
        });
    }
}
