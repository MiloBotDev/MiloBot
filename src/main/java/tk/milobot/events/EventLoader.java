package tk.milobot.events;

import tk.milobot.events.guild.OnGuildJoinEvent;
import tk.milobot.events.guild.OnGuildLeaveEvent;
import tk.milobot.main.JDAManager;

public class EventLoader {

    public static void load() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(
                new PingEvent(),
                new OnGuildJoinEvent(),
                new OnGuildLeaveEvent());
    }
}
