package org.samo_lego.fabrictailor.client.screen;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CustomPayloadC2SPacket;
import org.samo_lego.fabrictailor.util.TranslatedText;

interface SkinTabType {
    int width = 27;
    int height = 28;

    TranslatedText getTitle();
    TranslatedText getDescription();
    ItemStack getIcon();
    default boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }

    CustomPayloadC2SPacket getSkinChangePacket(String param, boolean useSlim);

    default boolean hasSkinModels() {
        return true;
    }

    default boolean showExplorerButton() {
        return false;
    }
}
