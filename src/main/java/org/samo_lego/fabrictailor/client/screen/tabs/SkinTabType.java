package org.samo_lego.fabrictailor.client.screen.tabs;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.world.item.ItemStack;

public interface SkinTabType {
    int width = 27;
    int height = 28;

    MutableComponent getTitle();

    MutableComponent getDescription();

    ItemStack getIcon();

    default boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }

    ServerboundCustomPayloadPacket getSkinChangePacket(String param, boolean useSlim);

    default boolean hasSkinModels() {
        return true;
    }

    default boolean showExplorerButton() {
        return false;
    }
}
