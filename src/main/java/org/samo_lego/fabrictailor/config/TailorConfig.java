package org.samo_lego.fabrictailor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static org.samo_lego.fabrictailor.FabricTailor.MOD_ID;
import static org.samo_lego.fabrictailor.FabricTailor.errorLog;

public class TailorConfig {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();


    @SerializedName("// Whether to allow players to have capes. WARNING! This will toggle ALL capes!")
    public final String _comment_allowCapes = "(default: true)";
    @SerializedName("allow_capes")
    public boolean allowCapes = true;

    @SerializedName("// Default skin for new players. Use command `/fabrictailor setDefaultSkin` to set those values.")
    public final String _comment_defaultSkin = "";
    @SerializedName("default_skin")
    public DefaultSkin defaultSkin = new DefaultSkin();

    @SerializedName("// How quickly can player change the skin, in seconds. -1 for no limit. If using this in server environment, -1 is not recommended.")
    public final String _comment_skinChangeTimer = "(default in singleplayer: -1, default for server: 60)";
    @SerializedName("skin_change_timer")
    public long skinChangeTimer = -1;

    public static class DefaultSkin {
        @SerializedName("// Whether to apply the default skin to ALL new players, not just those without skin.")
        public final String _comment_applyToAll = "(default: false)";
        @SerializedName("apply_to_all")
        public boolean applyToAll = false;
        public String value = "";
        public String signature = "";
    }

    /**
     * Loads config file.
     *
     * @param file file to load the language file from.
     * @return TaterzenLanguage object
     */
    public static TailorConfig loadConfigFile(File file, boolean serverEnvironment) {
        TailorConfig config;
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = gson.fromJson(fileReader, TailorConfig.class);
            } catch (IOException e) {
                throw new RuntimeException(MOD_ID + " Problem occurred when trying to load config: ", e);
            }
        }
        else {
            // Config doesn't exist yet
            config = new TailorConfig();
            if(serverEnvironment) {
                config.skinChangeTimer = 60;
            }
        }
        config.saveConfigFile(file);

        return config;
    }

    /**
     * Saves the config to the given file.
     *
     * @param file file to save config to
     */
    public void saveConfigFile(File file) {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            errorLog("Problem occurred when saving config: " + e.getMessage());
        }
    }
}
