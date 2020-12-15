package org.samo_lego.fabrictailor.client;

import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.TranslatableText;

public class SkinTab extends DrawableHelper {

    public enum TailorStyle {
        LOCAL("tab.fabrictailor.title_local"),
        URL("tab.fabrictailor.title_url"),
        PLAYER("tab.fabrictailor.title_player");

        private final String TITLE;

        TailorStyle(String title) {
            this.TITLE = title;
        }

        public String getTitle() {
            return this.TITLE;
        }
    }

    private final TranslatableText TITLE;
    private final TranslatableText DESCRIPTION;
    private final ItemStack ICON;
    private final int WIDTH;
    private final int HEIGHT;
    private String skinCommand = "/skin set ";

    public SkinTab(TailorStyle type, int width, int height) {
        this.TITLE = new TranslatableText(type.getTitle());
        this.WIDTH = width;
        this.HEIGHT = height;

        switch(type) {
            case PLAYER:
                this.ICON = new ItemStack(Items.PLAYER_HEAD);
                this.DESCRIPTION = new TranslatableText("description.fabrictailor.title_player");
                this.skinCommand += "player ";
                break;
            case LOCAL:
                this.ICON = new ItemStack(Items.MAGENTA_GLAZED_TERRACOTTA);
                this.DESCRIPTION = new TranslatableText("description.fabrictailor.title_local");
                this.skinCommand += "upload classic ";
                break;
            default:
                this.ICON = new ItemStack(Items.GLOBE_BANNER_PATTERN);
                this.DESCRIPTION = new TranslatableText("description.fabrictailor.title_url");
                this.skinCommand += "url classic ";
                break;
        }
    }

    public TranslatableText getTitle() {
        return this.TITLE;
    }

    public TranslatableText getDescription() {
        return this.DESCRIPTION;
    }
    public ItemStack getIcon() {
        return this.ICON;
    }

    public boolean isSelected(int startX, int startY, int mouseX, int mouseY) {
        return mouseX > startX && mouseX < startX + this.WIDTH && mouseY > startY && mouseY < startY + this.HEIGHT;
    }

    public String getSkinCommand() {
        return this.skinCommand;
    }
}
