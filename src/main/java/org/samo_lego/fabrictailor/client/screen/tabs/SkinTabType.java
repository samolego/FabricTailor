package org.samo_lego.fabrictailor.client.screen.tabs;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.samo_lego.fabrictailor.util.TranslatedText;

import java.util.Optional;

public interface SkinTabType {
    int width = 27;
    int height = 28;

    TranslatedText getTitle();

    TranslatedText getDescription();

    ItemStack getIcon();

    default boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }

    Optional<FriendlyByteBuf> getSkinChangePacket(String param, boolean useSlim);

    default boolean hasSkinModels() {
        return true;
    }

    default boolean showExplorerButton() {
        return false;
    }
}
