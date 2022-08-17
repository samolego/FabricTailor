package org.samo_lego.fabrictailor.client.screen.tabs;

import com.google.gson.Gson;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Base64;
import java.util.Optional;

public class CapeTab implements SkinTabType {
    private static final Gson gson = new Gson();

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
        String skinUrl = null;

        if (current.isPresent()) {
            String json = new String(Base64.getDecoder().decode(current.get().getValue()));
            skinUrl = gson.fromJson(json, MinecraftTexturesPayload.class).getTextures().get(MinecraftProfileTexture.Type.CAPE).getUrl();
        }

        var payload = new MinecraftTexturesPayload();
        payload.getTextures().put(MinecraftProfileTexture.Type.CAPE, new MinecraftProfileTexture(capeUrl, null));

        // Leave default skin
        payload.getTextures().put(MinecraftProfileTexture.Type.SKIN, new MinecraftProfileTexture(skinUrl, null));

        String json = gson.toJson(payload);
        String value = new String(Base64.getEncoder().encode(json.getBytes()));

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
