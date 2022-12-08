package tk.milobot.events;

import tk.milobot.main.JDAManager;

public class EventLoader {

    public static void load() {
        JDAManager.getInstance().getJDABuilder().addEventListeners(new PingEvent());
    }
}
