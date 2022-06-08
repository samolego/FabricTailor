package org.samo_lego.fabrictailor.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.samo_lego.fabrictailor.FabricTailor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.fabrictailor.FabricTailor.errorLog;

public class TextTranslations {
    private static final boolean SERVER_TRANSLATIONS_LOADED = FabricLoader.getInstance().isModLoaded("server_translations_api");
    private static final JsonObject LANG;

    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .disableHtmlEscaping()
            .create();

    public static MutableComponent create(String key, Object... args) {
        return Component.translatable(SERVER_TRANSLATIONS_LOADED ? key : (LANG.has(key) ? LANG.get(key).getAsString() : key), args);
    }

    static {
        JsonObject LANG1;
        InputStream langStream = FabricTailor.class.getResourceAsStream("/data/fabrictailor/lang/en_us.json");
        try {
            assert langStream != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(langStream, StandardCharsets.UTF_8))) {
                LANG1 = gson.fromJson(reader, JsonObject.class);
            }
        } catch (IOException e) {
            errorLog("Problem occurred when trying to load language: " + e.getMessage());
            LANG1 = new JsonObject();
        }
        LANG = LANG1;
    }
}
