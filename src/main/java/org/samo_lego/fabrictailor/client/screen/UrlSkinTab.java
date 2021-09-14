package org.samo_lego.fabrictailor.client.screen;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.samo_lego.fabrictailor.client.network.SkinChangePacket;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TranslatedText;

public class UrlSkinTab extends DrawableHelper implements SkinTabType {

    private final TranslatedText TITLE;
    private final TranslatedText DESCRIPTION;
    private final ItemStack ICON;

    public UrlSkinTab() {
        this.TITLE = new TranslatedText("tab.fabrictailor.title_url");
        this.DESCRIPTION = new TranslatedText("description.fabrictailor.title_url");
        this.ICON = new ItemStack(Items.GLOBE_BANNER_PATTERN);
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
    public CustomPayloadC2SPacket getSkinChangePacket(String url, boolean useSlim) {
        Property skinData = SkinFetcher.fetchSkinByUrl(url, useSlim);

        if(skinData == null)
            return null;
        return SkinChangePacket.create(skinData);
    }
}
