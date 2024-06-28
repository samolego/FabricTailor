package org.samo_lego.fabrictailor.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import org.samo_lego.config2brigadier.IBrigadierConfigurator;
import org.samo_lego.config2brigadier.annotation.BrigadierDescription;
import org.samo_lego.config2brigadier.annotation.BrigadierExcluded;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import static org.samo_lego.fabrictailor.FabricTailor.*;

public class TailorConfig implements IBrigadierConfigurator {
    private static final Gson gson = new GsonBuilder().serializeNulls().setPrettyPrinting().disableHtmlEscaping().create();


    @SerializedName("// Whether to allow players to have capes. WARNING! This will toggle ALL capes!")
    public final String _comment_allowCapes = "(default: true)";
    @BrigadierDescription(defaultOption = "true")
    @SerializedName("allow_capes")
    public boolean allowCapes = true;

    @SerializedName("// Default skin for new players. Use command `/fabrictailor setDefaultSkin` to set those values.")
    public final String _comment_defaultSkin = "";
    @SerializedName("default_skin")
    public DefaultSkin defaultSkin = new DefaultSkin();

    @SerializedName("// How quickly can player change the skin, in seconds. -1 for no limit. If using this in server environment, -1 is not recommended.")
    public final String _comment_skinChangeTimer = "(default in singleplayer: -1, default for server: 60)";
    @BrigadierDescription(defaultOption = "-1")
    @SerializedName("skin_change_timer")
    public long skinChangeTimer = -1;

    @SerializedName("// Custom skin server URL (takes priority over default minecraft usernames).")
    public final String _comment_customSkinServer0 = "";
    @SerializedName("// Leave empty to use default minecraft usernames (and ely.by fallback).")
    public final String _comment_customSkinServer1 = "";
    @SerializedName("// Available parameters: {player}. Example: https://skins.samolego.org/{player}.png")
    public final String _comment_customSkinServer2 = "";
    @BrigadierDescription(defaultOption = "")
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
            "duckduckgo.com",
            "discordapp.com"
    ));

    @Override
    public void save() {
        try (Writer writer = new OutputStreamWriter(new FileOutputStream(configFile), StandardCharsets.UTF_8)) {
            gson.toJson(this, writer);
        } catch (IOException e) {
            errorLog("Problem occurred when saving config: " + e.getMessage());
        }

    }

    public static class DefaultSkin {
        @SerializedName("// Whether to apply the default skin to ALL new players, not just those without skin.")
        public final String _comment_applyToAll = "(default: false)";
        @BrigadierDescription(defaultOption = "false")
        @SerializedName("apply_to_all")
        public boolean applyToAll = false;
        @BrigadierExcluded
        public String value = "";
        @BrigadierExcluded
        public String signature = "";
    }

    /**
     * Loads config file.
     *
     * @param file file to load the language file from.
     * @return TaterzenLanguage object
     */
    public static TailorConfig loadConfigFile(File file, boolean serverEnvironment) {
        TailorConfig config = null;
        if (file.exists()) {
            try (BufferedReader fileReader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)
            )) {
                config = gson.fromJson(fileReader, TailorConfig.class);
            } catch (IOException e) {
                errorLog(MOD_ID + " Problem occurred when trying to load config: " + e.getMessage());
            }
        }
        if(config == null) {
            // Config doesn't exist yet
            config = new TailorConfig();
            if(serverEnvironment) {
                // A bit different default config
                config.skinChangeTimer = 60;
            } else {
                config.defaultSkin.applyToAll = true;
            }
        }
        config.save();

        return config;
    }
}
