package io.github.milobotdev.milobot.events;

import io.github.milobotdev.milobot.events.guild.OnGuildJoinEvent;
import io.github.milobotdev.milobot.events.guild.OnGuildLeaveEvent;
import io.github.milobotdev.milobot.main.JDAManager;

public class EventLoader {

    public static void load() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(
                new PingEvent(),
                new OnGuildJoinEvent(),
                new OnGuildLeaveEvent());
    }
}
