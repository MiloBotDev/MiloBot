package games.hungergames.models;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Item {

    private final LuaValue chunk;
    private final String name;
    private final int rarity;

    public Item(Globals globals, String code) {
        this.chunk = globals.load(code).call();
        this.name = this.chunk.get("name").toString();
        this.rarity = this.chunk.get("rarity").toint();
    }

    public boolean onUse(Player player) {
        if (this.chunk.get("onUse").isnil()) {
            return false;
        }
        this.chunk.get("onUse").call(this.chunk, CoerceJavaToLua.coerce(player));
        return true;
    }

    // returns true if the player should be brought back to live
    public boolean onDeath(Player player) {
        if (this.chunk.get("onDeath").isnil()) {
            return false;
        }
        LuaValue result = this.chunk.get("onDeath").call(this.chunk, CoerceJavaToLua.coerce(player));
        if (result.isnil()) {
            return false;
        }
        return result.toboolean();
    }

    public String getName() {
        return name;
    }

    public int getRarity() {
        return rarity;
    }
}
