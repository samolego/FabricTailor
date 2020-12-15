package org.samo_lego.fabrictailor.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;

public class SkinTab extends DrawableHelper {

    private final TranslatableText TITLE;
    private final ItemStack ICON;
    private final int width;
    private final int height;

    public SkinTab(String title, ItemStack stack, int width, int height) {
        this.TITLE = new TranslatableText(title);
        this.ICON = stack;
        this.width = width;
        this.height = height;
    }

    public TranslatableText getTitle() {
        return this.TITLE;
    }
    public ItemStack getIcon() {
        return this.ICON;
    }

    public boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.width && mouseY > startY && mouseY < startY + this.height;
    }
}
