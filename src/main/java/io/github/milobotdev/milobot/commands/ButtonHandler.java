package io.github.milobotdev.milobot.commands;

import io.github.milobotdev.milobot.main.JDAManager;
import io.github.milobotdev.milobot.utility.Users;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

public class ButtonHandler {

    private static ButtonHandler instance;
    public enum DeferType {
        NONE, REPLY, EDIT
    }
    private record ButtonRecord(boolean onlyOnUserMatch, DeferType deferType, ExecutorService service, Consumer<ButtonInteractionEvent> action) {}
    private final HashMap<String, ButtonRecord> buttons = new HashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ButtonHandler.class);

    private ButtonHandler() {

    }

    public static synchronized ButtonHandler getInstance() {
        if (instance == null) {
            instance = new ButtonHandler();
        }
        return instance;
    }

    public void registerButton(String id, boolean onlyOnUserMatch, DeferType deferType,
                               ExecutorService service, Consumer<ButtonInteractionEvent> action) {
        buttons.put(id, new ButtonRecord(onlyOnUserMatch, deferType, service, action));
    }

    public void initialize() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(new ListenerAdapter() {
            @Override
            public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
                String[] id = event.getComponentId().split(":");
                if (id.length < 2) {
                    logger.warn("Button id is invalid: " + event.getComponentId());
                    return;
                }
                String authorId = id[0];
                String type = id[1];
                User user = event.getUser();
                Users.getInstance().addUserIfNotExists(event.getUser().getIdLong());

                if (buttons.containsKey(type)) {
                    ButtonRecord record = buttons.get(type);
                    if (authorId.equals(user.getId()) || !record.onlyOnUserMatch()) {
                        if (record.deferType() == DeferType.REPLY) {
                            event.deferReply().queue();
                        } else if (record.deferType() == DeferType.EDIT) {
                            event.deferEdit().queue();
                        }
                        record.service().execute(() -> {
                            try {
                                record.action().accept(event);
                            } catch (Exception e) {
                                logger.error("An exception occurred while handling button type: " + type, e);
                            }
                        });
                    }
                }
            }
        });
    }
}
