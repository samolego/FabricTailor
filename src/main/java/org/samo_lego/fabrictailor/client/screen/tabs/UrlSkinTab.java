package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TextTranslations;

import java.util.Optional;

public class UrlSkinTab extends GuiComponent implements SkinTabType {

    private final MutableComponent TITLE;
    private final MutableComponent DESCRIPTION;
    private final ItemStack ICON;

    public UrlSkinTab() {
        this.TITLE = TextTranslations.create("tab.fabrictailor.title_url");
        this.DESCRIPTION = TextTranslations.create("description.fabrictailor.title_url");
        this.ICON = new ItemStack(Items.GLOBE_BANNER_PATTERN);
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
    public Optional<FriendlyByteBuf> getSkinChangePacket(String url, boolean useSlim) {
        Property skinData = SkinFetcher.fetchSkinByUrl(url, useSlim);

        if (skinData == null)
            return Optional.empty();

        return Optional.of(SkinPackets.skin2ByteBuf(skinData));
    }
}
