package com.tesseract.core;

import com.google.gson.*;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {

    private static final File CONFIG_FILE =
            new File(Minecraft.getMinecraft().mcDataDir, "tesseract/config.json");

    private final Map<String, JsonObject> data = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public void load() {
        if (!CONFIG_FILE.exists()) return;
        try (Reader r = new FileReader(CONFIG_FILE)) {
            JsonObject root = new JsonParser().parse(r).getAsJsonObject();
            for (Map.Entry<String, JsonElement> entry : root.entrySet()) {
                data.put(entry.getKey(), entry.getValue().getAsJsonObject());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            CONFIG_FILE.getParentFile().mkdirs();
            try (Writer w = new FileWriter(CONFIG_FILE)) {
                JsonObject root = new JsonObject();
                data.forEach(root::add);
                gson.toJson(root, w);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JsonObject getSection(String key) {
        return data.computeIfAbsent(key, k -> new JsonObject());
    }

    public void setSection(String key, JsonObject obj) {
        data.put(key, obj);
    }
}