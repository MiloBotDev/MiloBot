package io.github.milobotdev.milobot.games.hungergames.model;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Event {

    private final LuaValue chunk;
    private final String name;
    private final int rarity;

    public Event(@NotNull Globals globals, String code) {
        this.chunk = globals.load(code).call();
        this.name = this.chunk.get("name").toString();
        this.rarity = this.chunk.get("rarity").toint();
    }

    public boolean onTrigger(Player player) {
        if (this.chunk.get("onTrigger").isnil()) {
            return false;
        }
        this.chunk.get("onTrigger").call(this.chunk, CoerceJavaToLua.coerce(player));
        return true;
    }

    public String getName() {
        return name;
    }

    public int getRarity() {
        return rarity;
    }
}
