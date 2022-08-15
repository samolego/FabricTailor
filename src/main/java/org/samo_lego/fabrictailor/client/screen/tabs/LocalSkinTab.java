package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TranslatedText;

import java.util.Optional;

public class LocalSkinTab extends GuiComponent implements SkinTabType {
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
    public Optional<FriendlyByteBuf> getSkinChangePacket(String filePath, boolean useSlim) {
        Property skinData = SkinFetcher.setSkinFromFile(filePath, useSlim);

        if (skinData == null)
            return Optional.empty();
        return Optional.of(SkinPackets.skin2ByteBuf(skinData));
    }

    @Override
    public boolean showExplorerButton() {
        return true;
    }
}
