package games.hungergames.models;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import utility.Config;

import java.io.Console;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Objects;

public class Item {
    private final String name;
    private ItemType type;
    private final int damage;
    private final int heal;
    private final int rarity;

    public Item(String name, JsonObject json) {
        this.name = name;

        String typeString = json.get("type").getAsString();
        for (ItemType type : ItemType.values()) {
            if (type.getName().equals(typeString)) {
                this.type = type;
            }
        }

        this.damage = json.has("damage") ? json.get("damage").getAsInt() : 0;
        this.heal = json.has("heal") ? json.get("heal").getAsInt() : 0;
        this.rarity = json.has("rarity") ? json.get("rarity").getAsInt() : 0;
    }

    public String getName() {
        return name;
    }

    public ItemType getType() {
        return type;
    }

    public int getDamage() {
        return damage;
    }

    public int getHeal() {
        return heal;
    }

    public int getRarity() {
        return rarity;
    }

    private final static HashMap<String, Item> items = new HashMap<>();

    public static Item getItem(String name) {
        return items.get(name);
    }
    public static void loadItems(JsonObject json) {
        json.entrySet().forEach(entry -> {
            items.put(entry.getKey(), new Item(entry.getKey(), entry.getValue().getAsJsonObject()));
        });
    }

    static {
        try {
            URI uri = Objects.requireNonNull(Item.class.getResource(Config.getInstance().getHungerGamesJsonPath())).toURI();
            if ("jar".equals(uri.getScheme())) {
                for (FileSystemProvider provider : FileSystemProvider.installedProviders()) {
                    if (provider.getScheme().equalsIgnoreCase("jar")) {
                        try {
                            provider.getFileSystem(uri);
                        } catch (FileSystemNotFoundException e) {
                            // in this case we need to initialize it first:
                            provider.newFileSystem(uri, Collections.emptyMap());
                        }
                    }
                }
            }
            Path source = Paths.get(uri);
            String jsonAsString = new String(Files.readAllBytes(source));
            JsonObject asJsonObject = JsonParser.parseString(jsonAsString).getAsJsonObject();
            loadItems(asJsonObject);
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
