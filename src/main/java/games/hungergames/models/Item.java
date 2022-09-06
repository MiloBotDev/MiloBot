package games.hungergames.models;

import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public class Item {

    private final LuaValue chunk;
    private final String name;
    private final int rarity;
    private final String type;
    private final int heal;
    private final int damage;

    public Item(@NotNull Globals globals, String code) {
        this.chunk = globals.load(code).call();
        this.name = this.chunk.get("name").toString();
        this.rarity = this.chunk.get("rarity").toint();
        this.type = this.chunk.get("type").toString();
        if (this.chunk.get("heals").isnil()) {
            this.heal = 0;
        } else {
            this.heal = this.chunk.get("heals").toint();
        }
        if (this.chunk.get("damage").isnil()) {
            this.damage = 0;
        } else {
            this.damage = this.chunk.get("damage").toint();
        }
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

    public String getType() {
        return type;
    }

    public int getHeal() {
        return heal;
    }

    public int getDamage() {
        return damage;
    }
}
