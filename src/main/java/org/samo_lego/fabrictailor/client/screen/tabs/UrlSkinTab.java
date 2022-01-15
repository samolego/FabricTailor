package org.samo_lego.fabrictailor.client.screen.tabs;

import com.mojang.authlib.properties.Property;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.samo_lego.fabrictailor.network.SkinPackets;
import org.samo_lego.fabrictailor.util.SkinFetcher;
import org.samo_lego.fabrictailor.util.TranslatedText;

public class UrlSkinTab extends GuiComponent implements SkinTabType {

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
    public ServerboundCustomPayloadPacket getSkinChangePacket(String url, boolean useSlim) {
        Property skinData = SkinFetcher.fetchSkinByUrl(url, useSlim);

        if(skinData == null)
            return null;
        return SkinPackets.createSkinChangePacket(skinData);
    }
}
