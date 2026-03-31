package org.samo_lego.fabrictailor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.samo_lego.fabrictailor.FabricTailor.*;
import static org.samo_lego.fabrictailor.util.Logging.error;

public class TailorConfig {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();


    @SerializedName("// Whether to allow players to have capes. WARNING! This will toggle ALL capes!")
    public final String _comment_allowCapes = "(default: true)";
    @SerializedName("allow_capes")
    public boolean allowCapes = true;
    
    public Logging logging = new Logging();
    
    public static class Logging {
        @SerializedName("// Whether to send (successful) command feedback for skin changes. Errors are sent regardless.")
        public final String _comment_skinChangeFeedback = "(default: true)";
        @SerializedName("skin_change_feedback")
        public boolean skinChangeFeedback = true;

        @SerializedName("// Whether to send debug messages to console.")
        public final String _comment_debug = "(default: false)";
        public boolean debug = false;
    }

    @SerializedName("// Default skin for new players. Use command `/fabrictailor setDefaultSkin` to set those values.")
    public final String _comment_defaultSkin = "";
    @SerializedName("default_skin")
    public DefaultSkin defaultSkin = new DefaultSkin();

    @SerializedName("// How quickly can player change the skin, in seconds. -1 for no limit. If using this in server environment, -1 is not recommended.")
    public final String _comment_skinChangeTimer = "(default in singleplayer: -1, default for server: 60)";
    @SerializedName("skin_change_timer")
    public long skinChangeTimer = -1;

    @SerializedName("// Custom skin server URL.")
    public final String _comment_customSkinServer0 = "";
    @SerializedName("// If not empty, you'll get another command /skin set custom, which will use this server.")
    public final String _comment_customSkinServer1 = "";
    @SerializedName("// Available parameters: {player}. Example: https://skins.samolego.org/{player}.png. Skins returned need to be 64x64!")
    public final String _comment_customSkinServer2 = "";
    @SerializedName("custom_skin_server")
    public String customSkinServer = "";

    @SerializedName("texture_allowed_domains")
    public Set<String> allowedTextureDomains = new HashSet<>(Set.of(
            "minecraft.net",
            "mojang.com",
            "crafatar.com",
            "imgur.com",
            "githubusercontent.com",
            "minecraftskins.com",
            "mc-heads.net",
            "ely.by",
            "namemc.com",
            "planetminecraft.com",
            "googleusercontent.com",
            "nocookie.net",
            "discord.com",
            "duckduckgo.com"
    ));

    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            error("Problem occurred when saving config: " + e.getMessage());
        }

    }

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
     * @return Tailor config object
     */
    public static TailorConfig loadConfigFile(File file, boolean serverEnvironment) {
        TailorConfig defaults = createDefault(serverEnvironment);

        if (!file.exists()) {
            return defaults;
        }

        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            TailorConfig loaded = gson.fromJson(reader, TailorConfig.class);
            if (loaded == null) {
                return defaults;
            }

            loaded.applyDefaults(defaults);
            return loaded;
        } catch (Exception e) {
            error("Problem occurred when loading config: " + e.getMessage());
            return defaults;
        }
    }

    public void reload(TailorConfig newConfig) {
        this.allowCapes = newConfig.allowCapes;
        this.logging = newConfig.logging;
        this.defaultSkin = newConfig.defaultSkin;
        this.skinChangeTimer = newConfig.skinChangeTimer;
        this.customSkinServer = newConfig.customSkinServer;
        this.allowedTextureDomains = new HashSet<>(newConfig.allowedTextureDomains);
    }

    private static TailorConfig createDefault(boolean serverEnvironment) {
        var config = new TailorConfig();
        if (serverEnvironment) {
            config.skinChangeTimer = 60;
        } else {
            config.defaultSkin.applyToAll = true;
        }

        return config;
    }

    private void applyDefaults(TailorConfig defaults) {
        if (this.logging == null) {
            this.logging = defaults.logging;
        }

        if (this.defaultSkin == null) {
            this.defaultSkin = defaults.defaultSkin;
        } else {
            if (this.defaultSkin.value == null) {
                this.defaultSkin.value = defaults.defaultSkin.value;
            }
            if (this.defaultSkin.signature == null) {
                this.defaultSkin.signature = defaults.defaultSkin.signature;
            }
        }

        if (this.customSkinServer == null) {
            this.customSkinServer = defaults.customSkinServer;
        }

        if (this.allowedTextureDomains == null || this.allowedTextureDomains.isEmpty()) {
            this.allowedTextureDomains = new HashSet<>(defaults.allowedTextureDomains);
        }
    }
}
