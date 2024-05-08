package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.payload.VanillaSkinPayload;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Optional;

public class LocalSkinTab implements SkinTabType {
    private final MutableComponent TITLE;
    private final MutableComponent DESCRIPTION;
    private final ItemStack ICON;

    public LocalSkinTab() {
        this.ICON = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        this.DESCRIPTION = TextTranslations.create("description.fabrictailor.title_local");
        this.TITLE = TextTranslations.create("tab.fabrictailor.title_local");
    }

    @Override
    public MutableComponent getTitle() {
        return this.TITLE;
    }

    @Override
    public MutableComponent getDescription() {
        return this.DESCRIPTION;
    }

    @Override
    public ItemStack getIcon() {
        return this.ICON;
    }

    @Override
    public Optional<CustomPacketPayload> getSkinChangePacket(LocalPlayer player, String filePath, boolean useSlim) {
        Property skinData = SkinFetcher.setSkinFromFile(filePath, useSlim);

        if (skinData == null)
            return Optional.empty();
        return Optional.of(new VanillaSkinPayload(skinData));
    }

    @Override
    public boolean showExplorerButton() {
        return true;
    }
}
