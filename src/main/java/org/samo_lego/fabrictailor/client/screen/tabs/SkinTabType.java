package org.samo_lego.fabrictailor.client.screen.tabs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public interface SkinTabType {
    int width = 27;
    int height = 28;

    MutableComponent getTitle();

    MutableComponent getDescription();

    ItemStack getIcon();

    default boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }

    Optional<Pair<ResourceLocation, FriendlyByteBuf>> getSkinChangePacket(LocalPlayer player, String param, boolean useSlim);

    default boolean hasSkinModels() {
        return true;
    }

    default boolean showExplorerButton() {
        return false;
    }

    default boolean showModelBackwards() {
        return false;
    }

    default Property getExtendedProperty(Player player, MinecraftProfileTexture.Type type, String textureUrl, JsonObject metadata) {
        var current = player.getGameProfile().getProperties().get(SkinManager.PROPERTY_TEXTURES).stream().findFirst();
        String json;
        if (current.isEmpty()) {
            // No skin is set
            json = "{\"textures\":{}}";
        } else {
            json = new String(Base64.getDecoder().decode(current.get().getValue()), StandardCharsets.UTF_8);
        }
        JsonObject jsonPayload = JsonParser.parseString(json).getAsJsonObject();
        JsonObject textures = jsonPayload.get("textures").getAsJsonObject();

        if (textures.has(type.toString())) {
            JsonObject texture = textures.get(type.toString()).getAsJsonObject();
            if (texture.has("url")) {
                texture.remove("url");
            }
            texture.addProperty("url", textureUrl);
        } else {
            JsonObject cape = new JsonObject();
            cape.addProperty("url", textureUrl);
            textures.add(type.toString(), cape);
        }

        JsonObject texture = textures.get(type.toString()).getAsJsonObject();
        if (texture.has("metadata")) {
            texture.remove("metadata");
        }

        if (metadata != null) {
            texture.add("metadata", metadata);
        }

        String value = new String(Base64.getEncoder().encode(jsonPayload.toString().getBytes()));

        return new Property(SkinManager.PROPERTY_TEXTURES, value);
    }
}
