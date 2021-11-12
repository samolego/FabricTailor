package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TranslatedText;

public class LocalSkinTab extends DrawableHelper implements SkinTabType {
    private final TranslatedText TITLE;
    private final TranslatedText DESCRIPTION;
    private final ItemStack ICON;

    public LocalSkinTab() {
        this.ICON = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
        this.DESCRIPTION = new TranslatedText("description.fabrictailor.title_local");
        this.TITLE = new TranslatedText("tab.fabrictailor.title_local");
    }

    @Override
    public TranslatedText getTitle() {
        return this.TITLE;
    }

    @Override
    public TranslatedText getDescription() {
        return this.DESCRIPTION;
    }

    @Override
    public ItemStack getIcon() {
        return this.ICON;
    }

    @Override
    public CustomPayloadC2SPacket getSkinChangePacket(String filePath, boolean useSlim) {
        Property skinData = SkinFetcher.setSkinFromFile(filePath, useSlim);

        if(skinData == null)
            return null;
        return SkinPackets.createSkinChangePacket(skinData);
    }

    @Override
    public boolean showExplorerButton() {
        return true;
    }
}
