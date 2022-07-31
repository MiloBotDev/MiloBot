package games.hungergames.models;

import games.hungergames.HungerGames;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

import java.util.HashMap;
import java.util.Random;

public class Item {
    private final LuaValue chunk;
    private final String name;
    private final int rarity;

    public Item(Globals globals, String code) {
        this.chunk = globals.load(code).call();
        this.name = this.chunk.get("name").toString();
        this.rarity = this.chunk.get("rarity").toint();
    }

    public void use(Player player) {

    }

    public String getName() {
        return name;
    }

    public int getRarity() {
        return rarity;
    }
}
