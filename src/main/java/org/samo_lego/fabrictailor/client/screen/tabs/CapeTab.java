package org.samo_lego.fabrictailor.client.screen.tabs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

public class CapeTab implements SkinTabType {
    private final Gson gson = new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();

    @Override
    public MutableComponent getTitle() {
        return TextTranslations.create("tab.fabrictailor.title_cape");
    }

    @Override
    public MutableComponent getDescription() {
        return TextTranslations.create("description.fabrictailor.title_cape");
    }

    @Override
    public ItemStack getIcon() {
        return Items.LIME_BANNER.getDefaultInstance();
    }

    @Override
    public Optional<FriendlyByteBuf> getSkinChangePacket(LocalPlayer player, String capeUrl, boolean useSlim) {
        var current = player.getGameProfile().getProperties().get("textures").stream().findFirst();
        String json;
        if (current.isEmpty()) {
            json = "{\"textures\":{}}";
        } else {
            json = new String(Base64.getDecoder().decode(current.get().getValue()), StandardCharsets.UTF_8);
        }
        JsonObject jsonPayload = JsonParser.parseString(json).getAsJsonObject();

        JsonObject textures = jsonPayload.get("textures").getAsJsonObject();

        if (textures.has("CAPE")) {
            JsonObject cape = textures.get("CAPE").getAsJsonObject();
            if (cape.has("url")) {
                cape.remove("url");
            }
            cape.addProperty("url", capeUrl);
        } else {
            JsonObject cape = new JsonObject();
            cape.addProperty("url", capeUrl);
            textures.add("CAPE", cape);
        }

        String value = new String(Base64.getEncoder().encode(jsonPayload.toString().getBytes()));

        return Optional.of(SkinPackets.skin2ByteBuf(new Property("textures", value)));
    }

    @Override
    public boolean hasSkinModels() {
        return false;
    }

    @Override
    public boolean showModelBackwards() {
        return true;
    }
}
