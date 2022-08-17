package org.samo_lego.fabrictailor.client.screen.tabs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.datafixers.util.Pair;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Optional;
import java.util.UUID;

import static org.samo_lego.fabrictailor.network.SkinPackets.FABRICTAILOR_HD_CHANGE;

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
    public Optional<Pair<ResourceLocation, FriendlyByteBuf>> getSkinChangePacket(LocalPlayer player, String capeUrl, boolean useSlim) {
        var skinData = this.getExtendedProperty(player, MinecraftProfileTexture.Type.CAPE, capeUrl);
        return Optional.of(new Pair<>(FABRICTAILOR_HD_CHANGE, SkinPackets.skin2ByteBuf(skinData)));
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
