package org.samo_lego.fabrictailor.client.screen.tabs;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.screens.advancements.AdvancementTabType;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.fabrictailor.casts.TailoredPlayer;
import org.samo_lego.fabrictailor.mixin.client.AAbstractClientPlayer;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

public interface SkinTabType {
    int width = 27;
    int height = 28;

    Component getTitle();

    Component getDescription();

    ItemStack getIcon();

    default boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }

    Optional<CustomPacketPayload> getSkinChangePacket(LocalPlayer player, String param, boolean useSlim);

    default boolean hasSkinModels() {
        return true;
    }

    default boolean showExplorerButton() {
        return false;
    }

    default boolean showModelBackwards() {
        return false;
    }

    default Property getExtendedProperty(LocalPlayer player, MinecraftProfileTexture.Type type, String textureUrl, JsonObject metadata) {
        var current = ((AAbstractClientPlayer) player).ft_getPlayerInfo().getProfile().getProperties().get(TailoredPlayer.PROPERTY_TEXTURES).stream().findFirst();
        String json = current.map(property -> new String(Base64.getDecoder().decode(property.value()), StandardCharsets.UTF_8))
                .orElse("{\"" + TailoredPlayer.PROPERTY_TEXTURES + "\":{}}");
        JsonObject jsonPayload = JsonParser.parseString(json).getAsJsonObject();
        JsonObject textures = jsonPayload.get(TailoredPlayer.PROPERTY_TEXTURES).getAsJsonObject();

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

        return new Property(TailoredPlayer.PROPERTY_TEXTURES, value);
    }

    default AdvancementTabType getTabType() {
        return AdvancementTabType.ABOVE;
    }
}
